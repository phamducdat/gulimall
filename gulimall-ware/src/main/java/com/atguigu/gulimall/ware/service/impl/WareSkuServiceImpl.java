package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.OrderVo;
import com.atguigu.gulimall.ware.vo.SkuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private OrderFeignService orderFeignService;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // Unlock stock based on OrderTo object
    @Transactional
    @Override
    public void unlockStock(OrderTo orderTo) {
        // Get order number
        String orderSn = orderTo.getOrderSn();
        // Query inventory work order based on order number
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        // Get inventory work order id
        Long taskId = taskEntity.getId();
        // Query all inventory work order details with "lockStatus = 1" based on inventory work order id
        //   lockStatus = 1 : Stock locked
        //   lockStatus = 2 : Stock unlocked
        //   lockStatus = 3 : Stock deducted
        QueryWrapper<WareOrderTaskDetailEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId);
        wrapper.eq("lock_status", 1);
        List<WareOrderTaskDetailEntity> wareOrderTaskDetailEntities = wareOrderTaskDetailService.list(wrapper);

        // Iterate through the inventory work order detail collection and unlock stock
        for (WareOrderTaskDetailEntity entity : wareOrderTaskDetailEntities) {
            // Unlock stock & update inventory work order detail
            unLockStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum(), entity.getId());
        }
    }


    // Unlock stock based on StockLockedTo object
    @Override
    public void unlockStock(StockLockedTo stockLockedTo) {
        // Get inventory work order detail
        StockDetailTo detail = stockLockedTo.getDetailTo();
        // Get inventory work order detail id
        Long detailId = detail.getId();

        /*
         * Query the latest inventory work order detail based on the inventory work order detail id (corresponding to the wms_ware_order_task_detail database table)
         *     Record found: Stock locked successfully (the "inventory work order detail" was created when "locking stock", finding the "inventory work order detail" record indicates successful stock locking)
         *     Record not found: Stock lock failed (the "inventory work order detail" was created when "locking stock", not finding the "inventory work order detail" record indicates stock lock failed, transaction rolled back)
         *
         *     1. Record found: Stock locked successfully -> Decide whether to unlock stock based on order status & inventory work order detail lock status
         *         a. Order does not exist: Unlock stock
         *             After the inventory service completes "locking stock", the order service did not receive the inventory service's response OR there was a problem in the order service's subsequent operations of "creating order"
         *             The order service throws an exception and the order service transaction rolls back
         *             Since the inventory service has completed, the inventory service transaction will not roll back
         *             Thus, there is no order service-related data record in the database, but there is inventory service-related data record
         *             In this case (order creation failure), stock must be unlocked
         *         b. Order status is "canceled" & inventory work order detail lock status is "stock locked": Order has been canceled, but stock is still locked -> Unlock stock
         *     2. Record not found: Stock lock failed, transaction rolled back -> No need to unlock
         */

        // Query the latest inventory work order detail based on the inventory work order detail id
        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = wareOrderTaskDetailService.getById(detailId);
        if (wareOrderTaskDetailEntity != null) {
            // Record found: Stock locked successfully -> Decide whether to unlock stock based on order status & inventory work order detail lock status
            // Inventory work order id
            Long id = stockLockedTo.getId();
            // Get inventory work order based on inventory work order id
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            // Get order number from inventory work order
            String orderSn = taskEntity.getOrderSn();
            // Query order information based on order number
            R orderVoR = orderFeignService.getOrderByOrderSn(orderSn);
            if (orderVoR.getCode() == 0) {
                // Remote service call successful
                // Get orderVo (order information) from orderVoR
                OrderVo orderVo = orderVoR.getData(new TypeReference<OrderVo>() {
                });
                // orderVo == null          : Order does not exist
                // orderVo.getStatus() == 4 : Order status is "canceled"
                // Order does not exist: Unlock stock
                // Order status is "canceled" & inventory work order detail lock status is "stock locked": Order has been canceled, but stock is still locked -> Unlock stock
                if (orderVo == null || orderVo.getStatus() == 4) {
                    // Get inventory work order detail lock status
                    //   lockStatus = 1 : Stock locked
                    //   lockStatus = 2 : Stock unlocked
                    //   lockStatus = 3 : Stock deducted
                    // Only when lockStatus = 1, stock needs to be unlocked (stock is locked, but needs to be unlocked)
                    if (wareOrderTaskDetailEntity.getLockStatus() == 1) {
                        // Unlock stock & update inventory work order detail
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                }
            } else {
                // Remote service call failed -> Throw exception -> Message listener handleStockLockedRelease() catches the exception -> Reject the message -> Message requeued
                throw new RuntimeException("Remote service call failed");
            }
        } else {
            // Record not found: Stock lock failed, transaction rolled back -> No need to unlock
        }
    }


    // Unlock stock & update inventory work order detail
    public void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        // Unlock stock (unlock stock based on product id, warehouse id, and quantity of product to be unlocked)
        baseMapper.unLockStock(skuId, wareId, num);

        // Update inventory work order detail
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        // Set inventory work order detail id
        entity.setId(taskDetailId);
        // Set lock status to unlocked
        entity.setLockStatus(2);
        // Update inventory work order detail
        wareOrderTaskDetailService.updateById(entity);
    }

    /**
     * Lock stock (only successful if all order items are locked successfully; if any order item fails to lock, the lock is considered a failure)
     */
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo lockVo) {
        /*
         * Inventory work order & inventory work order details
         *
         * One order corresponds to one inventory work order
         * One order has n order items
         * One order item corresponds to one inventory work order detail (n order items correspond to n inventory work order details)
         *
         * The inventory work order saves the order number
         * The inventory work order detail saves relevant information for each order item, including inventory work order id, product id, locked product quantity, warehouse id, lock status
         *
         * What is the purpose of the inventory work order & inventory work order details?
         *   When unlocking stock, by querying the inventory work order & inventory work order details,
         *   we can get relevant information for each order item, including inventory work order id, product id, locked product quantity, warehouse id, lock status
         *   This information is needed to unlock the stock
         *
         * For the inventory work order, as long as the current method is executed, the "inventory work order" information is saved in the database first
         * For the inventory work order detail, each order item locked successfully saves an "inventory work order detail" information in the database
         */

        // Save inventory work order
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(lockVo.getOrderSn());
        wareOrderTaskService.save(taskEntity);

        // For each order item, encapsulate a SkuWareHasStock object (query which warehouses have stock for each product)
        List<OrderItemVo> locks = lockVo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            // Query which warehouses have stock for the product based on skuId
            List<Long> wareIds = baseMapper.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        // Lock stock
        for (SkuWareHasStock hasStock : collect) {
            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                // No warehouses have the current product -> current product lock failed -> global lock failed -> throw NoStockException
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                // Lock stock (lock stock based on product id, warehouse id, and quantity of product to be locked)
                //   Lock success: return 1
                //   Lock failure: return 0
                Long count = baseMapper.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    // Current product lock successful
                    skuStocked = true;

                    // Save inventory work order detail
                    WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity();
                    taskDetailEntity.setSkuId(skuId);
                    taskDetailEntity.setSkuName("");
                    taskDetailEntity.setSkuNum(hasStock.getNum());
                    taskDetailEntity.setTaskId(taskEntity.getId());
                    taskDetailEntity.setWareId(wareId);
                    taskDetailEntity.setLockStatus(1);
                    wareOrderTaskDetailService.save(taskDetailEntity);

                    // Encapsulate StockLockedTo object (inventory work order TO)
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(taskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(taskDetailEntity, stockDetailTo);
                    stockLockedTo.setDetailTo(stockDetailTo);

                    /*
                     * One order corresponds to one inventory work order
                     * One order has n order items
                     * One order item corresponds to one inventory work order detail (n order items correspond to n inventory work order details)
                     *
                     * If all order items are locked successfully, the database will save one "inventory work order" record and n "inventory work order detail" records
                     * At the same time, RabbitMQ will receive n messages, each message containing a StockLockedTo object
                     *
                     * If any order item fails to lock, the system will throw a NoStockException, and the transaction will roll back
                     * The database will not have "inventory work order" and "inventory work order detail" records
                     * However, RabbitMQ will receive messages for all successfully locked order items before the lock failed
                     * For the messages already sent, after 2 minutes, they will be forwarded to the "stock.release.stock.queue" queue
                     * The consumer of the "stock.release.stock.queue" will execute the "unlock stock" business logic after receiving these messages
                     * It will determine that the "inventory work order" and "inventory work order detail" records corresponding to these messages do not exist, so no unlock is needed
                     */

                    // Send delayed message
                    // The "stock-event-exchange" exchange will dispatch the message to the "stock.delay.queue"
                    // The "stock.delay.queue" is a delayed queue, also called a dead letter queue (the "stock.delay.queue" has no consumers)
                    // After 2 minutes, the delayed queue will forward the message to the "stock.release.stock.queue"
                    // The consumer of the "stock.release.stock.queue" will execute the "unlock stock" business logic after receiving the message
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);

                    break;
                } else {
                    // Current warehouse failed to lock the current product
                    // If there is another warehouse, try to lock the current product with the next warehouse
                }
            }
            if (!skuStocked) {
                // All warehouses failed to lock the current product -> current product lock failed -> global lock failed -> throw NoStockException
                throw new NoStockException(skuId);
            }
        }
        // All products locked successfully -> global lock successful
        return true;
    }


    // Check if SKU has stock
    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            // Query the total stock of the current SKU
            Long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock(count != null && count > 0);
            return vo;
        }).collect(Collectors.toList());

        return collect;
    }


    // Add stock for successfully purchased products
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // If there is no record of this product in the warehouse -> add new
        // If there is already a record of this product in the warehouse -> update
        List<WareSkuEntity> entities = wareSkuDao.selectList(
                new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId)
        );
        if (entities == null || entities.isEmpty()) {
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);

            // Remote call to product microservice to query product name
            // If the query fails, the transaction does not roll back!!! Because it is not necessary to roll back the entire transaction just because a non-essential field could not be queried
            // Therefore, if an exception is caught, there is no need to throw the exception, just swallow the exception -> in the catch{} block, apart from printing the exception information, do nothing else!
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Add product stock record
            wareSkuDao.insert(skuEntity);
        } else {
            // Update product stock record
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }


    // Paginated conditional query
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();

        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    @Data
    class SkuWareHasStock {
        // Product id
        private Long skuId;
        // Quantity of product to be locked
        private Integer num;
        // Collection of warehouses that have the product (which warehouses have stock for this product)
        private List<Long> wareId;
    }

}
