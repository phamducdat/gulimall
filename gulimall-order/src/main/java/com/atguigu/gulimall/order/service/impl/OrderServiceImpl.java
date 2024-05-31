package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.SecKillOrderTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResponseVO;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.PaymentInfoEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.service.PaymentInfoService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    // ThreadLocal : 同一个线程共享数据
    // 将 OrderSubmitVo对象 存入 ThreadLocal 中
    private ThreadLocal<OrderSubmitVo> submitVoThreadLocal = new InheritableThreadLocal<>();

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private PaymentInfoService paymentInfoService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // Create flash sale order
    @Override
    public void createSecKillOrder(SecKillOrderTo secKillOrderTo) {
        // Retrieve spu information based on skuId
        R spuInfoR = productFeignService.getSpuInfoBySkuId(secKillOrderTo.getSkuId());
        SpuInfoVo spuInfoVo = spuInfoR.getData(new TypeReference<SpuInfoVo>() {
        });

        // Save order information
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(secKillOrderTo.getOrderSn());
        orderEntity.setMemberId(secKillOrderTo.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal orderPrice = secKillOrderTo.getSeckillPrice().multiply(new BigDecimal("" + secKillOrderTo.getNum()));
        orderEntity.setPayAmount(orderPrice);
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        // Save order item information
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(secKillOrderTo.getOrderSn());
        orderItemEntity.setRealAmount(orderPrice);
        orderItemEntity.setSkuQuantity(secKillOrderTo.getNum());
        orderItemEntity.setSpuId(spuInfoVo.getId());
        orderItemEntity.setSpuName(spuInfoVo.getSpuName());
        orderItemEntity.setSpuBrand(spuInfoVo.getBrandId() + "");
        orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());
        orderItemEntity.setSkuId(secKillOrderTo.getSkuId());
        orderItemService.save(orderItemEntity);
    }

    // Payment completed -> Save payment information & modify order status
    @Override
    public String handPayResult(PayAsyncVo payAsyncVo) {
        // Save payment information - corresponds to the oms_payment_info database table
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setAlipayTradeNo(payAsyncVo.getTrade_no());
        paymentInfoEntity.setOrderSn(payAsyncVo.getOut_trade_no());
        paymentInfoEntity.setPaymentStatus(payAsyncVo.getTrade_status());
        paymentInfoEntity.setCallbackTime(payAsyncVo.getNotify_time());
        paymentInfoService.save(paymentInfoEntity);

        // Modify order status
        if (payAsyncVo.getTrade_status().equals("TRADE_SUCCESS") || payAsyncVo.getTrade_status().equals("TRADE_FINISHED")) {
            // Order number
            String outTradeNo = payAsyncVo.getOut_trade_no();
            // Modify order status
            baseMapper.updateOrderStatus(outTradeNo, OrderStatusEnum.PAYED.getCode());
        }

        return "success";
    }


    // 分页查询当前登录用户的所有订单信息
    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        // 从 ThreadLocal 中获取 memberResponseVO
        MemberResponseVO memberResponseVO = LoginUserInterceptor.loginUser.get();
        // 分页查询
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", memberResponseVO.getId()).orderByDesc("id")
        );
        // 遍历 分页查询出来的所有订单信息
        List<OrderEntity> collect = page.getRecords().stream().map(order -> {
            // 查询 当前订单的所有订单项
            QueryWrapper<OrderItemEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("order_sn", order.getOrderSn());
            List<OrderItemEntity> itemEntities = orderItemService.list(wrapper);
            // 设置 当前订单对象 的 订单项
            order.setOrderItems(itemEntities);
            return order;
        }).collect(Collectors.toList());
        page.setRecords(collect);
        return new PageUtils(page);
    }

    // 根据 订单号 获取 PayVo对象
    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        // 根据 订单号 查询 订单信息
        OrderEntity order = this.getOrderByOrderSn(orderSn);
        // 订单金额 (保留2位小数 向上取值)
        BigDecimal orderPrice = order.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        // 设置 订单金额
        payVo.setTotal_amount(orderPrice.toString());
        // 设置 订单号
        payVo.setOut_trade_no(orderSn);
        // 根据 订单号 查询 订单项信息
        QueryWrapper<OrderItemEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("order_sn", orderSn);
        List<OrderItemEntity> list = orderItemService.list(wrapper);
        OrderItemEntity itemEntity = list.get(0);
        // 设置 订单标题
        payVo.setSubject(itemEntity.getSkuName());
        // 设置 商品描述
        payVo.setBody(itemEntity.getSkuAttrsVals());
        return payVo;
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        // Fetch the latest order status
        // If the latest order status is "Pending Payment", it means the user hasn't paid within the allotted time
        // In a real production environment, this time frame is typically 15-30 minutes. For testing purposes, it's set to 1 minute.
        // If the order status is "Pending Payment", the system assumes the user has abandoned the transaction
        // The system will then change the order status to "Canceled" and unlock the inventory.
        OrderEntity orderEntity = this.getById(entity.getId());

        // Check if the order's latest status is "Pending Payment"
        if (OrderStatusEnum.CREATE_NEW.getCode().equals(orderEntity.getStatus())) {
            // Update the order status to "Canceled"
            OrderEntity update = new OrderEntity();
            update.setId(orderEntity.getId());
            update.setStatus(OrderStatusEnum.CANCELLED.getCode());
            this.updateById(update);

            // Send a message to RabbitMQ to unlock inventory
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            try {
                // Send message
                // The "order-event-exchange" exchange will route the message to the "stock.release.stock.queue" queue
                // The consumer of "stock.release.stock.queue" will then execute the "unlock inventory" business logic
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (AmqpException e) {
                // If an exception is caught, it is likely due to network issues preventing the message from being sent successfully
                // -> Retry sending the failed message
                e.printStackTrace();
            }
        }
    }


    // Query order information based on order number
    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        QueryWrapper<OrderEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("order_sn", orderSn);
        OrderEntity orderEntity = this.getOne(wrapper);
        return orderEntity;
    }

    /**
     * Submit order (place order)
     * <p>
     * Under the high concurrency scene (E.G. Generation order), it is not suitable for using Seata to solve distributed transactions
     * In the high concurrency scene, we should use "flexible transaction -reliable message + final consistency scheme (asynchronous guarantee type)" solution to solve distributed transactions
     */
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo) {
        // Store orderSubmitVo in ThreadLocal
        submitVoThreadLocal.set(orderSubmitVo);

        SubmitOrderResponseVo response = new SubmitOrderResponseVo();
        // Initialize code = 0
        response.setCode(0);
        // Get memberResponseVO from ThreadLocal (memberResponseVO encapsulates user login information)
        MemberResponseVO memberResponseVO = LoginUserInterceptor.loginUser.get();

        /*
         * Verify the idempotency token (to prevent duplicate order submissions and ensure interface idempotency)
         *
         * Verification process:
         *   Step 1: Retrieve the idempotency token from Redis using the user ID
         *   Step 2: Compare the idempotency token retrieved from Redis with the token provided by the client
         *           If they are the same => Delete the idempotency token from Redis and return 1
         *           If they are different => Directly return 0
         *
         * NOTICE: The entire verification process must ensure atomicity
         *
         * Using a Lua script, you can achieve atomicity for the entire verification process
         *   String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
         */

        // Verify the idempotency token
        // Using a Lua script, you can achieve atomicity for the entire verification process
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        // Retrieve the idempotency token provided by the client
        String orderToken = orderSubmitVo.getOrderToken();
        // Atomically verify and delete the token
        //   First parameter: Lua script
        //   Second parameter: Values for KEYS in the Lua script
        //   Third parameter: Values for ARGV in the Lua script
        Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVO.getId()), orderToken);
        if (execute == 0L) {
            // Idempotency token verification failed
            response.setCode(1);
            return response;
        } else {
            // Idempotency token verification succeeded

            // 1. Create the order
            OrderCreateTo order = createOrder();

            // 2. Verify the price
            // Accurate price: Calculated order price (total payable amount = order total amount + shipping fee)
            BigDecimal payAmount = order.getOrder().getPayAmount();
            // Price provided by the client
            BigDecimal payPrice = orderSubmitVo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // Price verification succeeded

                // 3. Save the order and order items
                saveOrder(order);

                // 4. Lock inventory (all order items must be successfully locked, if any item fails to lock, the whole process fails)
                // Inventory lock VO
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> locks = order.getItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(locks);

                // Call remote microservice to lock inventory (all order items must be successfully locked, if any item fails to lock, the whole process fails)
                R r = wareFeignService.orderLockStock(wareSkuLockVo);

                if (r.getCode() == 0) {
                    // Inventory lock succeeded
                    response.setOrder(order.getOrder());

                    // Simulate a business exception
                    // int num = 10 / 0;

                    // Send a delayed message
                    // The "order-event-exchange" exchange will route the message to the "order.delay.queue" queue
                    // The "order.delay.queue" queue is a delayed queue, also called a dead letter queue (the "order.delay.queue" queue has no consumers)
                    // After 1 minute, the delayed queue will forward the message to the "order.release.order.queue" queue
                    // The consumer of the "order.release.order.queue" will check if the order status is "pending payment", if it is, it will cancel the order
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());

                    return response;
                } else {
                    // Inventory lock failed
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
                }
            } else {
                // Price verification failed
                response.setCode(2);
                return response;
            }
        }
    }


    // Create Order
    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        // Generate order number
        String orderSn = IdWorker.getTimeId();
        // Build order information
        OrderEntity orderEntity = buildOrder(orderSn);
        // Build all order items information
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);
        // Calculate order price (total payable amount = order total amount + shipping fee)
        computePrice(orderEntity, itemEntities);

        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setItems(itemEntities);

        return orderCreateTo;
    }

    // Build order information
    private OrderEntity buildOrder(String orderSn) {
        // Get memberResponseVO from ThreadLocal (memberResponseVO encapsulates user login information)
        MemberResponseVO memberResponseVO = LoginUserInterceptor.loginUser.get();

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberId(memberResponseVO.getId());

        // Get orderSubmitVo from ThreadLocal
        OrderSubmitVo orderSubmitVo = submitVoThreadLocal.get();
        // Calculate shipping fee based on the delivery address
        R fareR = wareFeignService.getFare(orderSubmitVo.getAddrId());
        if (fareR.getCode() == 0) {
            // Remote call successful
            // Get fareResp from fareR
            FareVo fareResp = fareR.getData(new TypeReference<FareVo>() {
            });
            // Set shipping fee information
            orderEntity.setFreightAmount(fareResp.getFare());
            // Set recipient information
            orderEntity.setReceiverCity(fareResp.getAddress().getCity());
            orderEntity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
            orderEntity.setReceiverName(fareResp.getAddress().getName());
            orderEntity.setReceiverPhone(fareResp.getAddress().getPhone());
            orderEntity.setReceiverPostCode(fareResp.getAddress().getPostCode());
            orderEntity.setReceiverProvince(fareResp.getAddress().getProvince());
            orderEntity.setReceiverRegion(fareResp.getAddress().getRegion());
        }
        // Set order status information
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        // Set auto-confirmation time to 7 days
        orderEntity.setAutoConfirmDay(7);
        // Set delete status to not deleted
        orderEntity.setDeleteStatus(0);
        return orderEntity;
    }


    // Build information for all order items
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        // Get all shopping items selected by the current user
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && !currentUserCartItems.isEmpty()) {
            return currentUserCartItems.stream().map(cartItem -> {
                // Build information for the current order item (build information for each order item)
                OrderItemEntity orderItemEntity = buildOrderItem(cartItem);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
        }
        return null;
    }


    // Build each order item information
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();

        // 1. Product spu information
        Long skuId = cartItem.getSkuId();
        // Query spu information based on skuId
        R spuInfoR = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo spuInfoVo = spuInfoR.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(spuInfoVo.getId());
        itemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        itemEntity.setSpuName(spuInfoVo.getSpuName());
        itemEntity.setCategoryId(spuInfoVo.getCatalogId());

        // 2. Product sku information
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        // Convert List collection to String
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());

        // 3. Product discount information
        // ...

        // 4. Product integration information
        BigDecimal total = cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount()));
        itemEntity.setGiftGrowth(total.intValue());
        itemEntity.setGiftIntegration(total.intValue());

        // 5. Current order item price information (Confirm the price of each order item before payment)
        // Product promotion split amount
        itemEntity.setPromotionAmount(BigDecimal.ZERO);
        // Coupon discount split amount
        itemEntity.setCouponAmount(BigDecimal.ZERO);
        // Integration discount split amount
        itemEntity.setIntegrationAmount(BigDecimal.ZERO);
        // The split amount after discounts (the actual amount of the current order item)
        // Total = unit price * quantity
        BigDecimal origin = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity()));
        // Payment amount = total - promotion amount - coupon amount - integration discount
        BigDecimal orderItemFinalPrice = origin.subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(orderItemFinalPrice);

        return itemEntity;
    }


    // Calculate order price (total payable amount = order total amount + shipping fee)
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        // Order total amount = sum of amounts of all order items
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal gift = new BigDecimal("0.0");
        BigDecimal growth = new BigDecimal("0.0");
        for (OrderItemEntity entity : itemEntities) {
            // Promotion discount amount for the item
            promotion = promotion.add(entity.getPromotionAmount());
            // Coupon discount amount for the item
            coupon = coupon.add(entity.getCouponAmount());
            // Integration discount amount for the item
            integration = integration.add(entity.getIntegrationAmount());
            // The actual amount of the item after discounts
            total = total.add(entity.getRealAmount());
            // Gift integration points
            gift = gift.add(new BigDecimal(entity.getGiftIntegration().toString()));
            // Gift growth value
            growth = growth.add(new BigDecimal(entity.getGiftGrowth().toString()));
        }
        // Set order total amount
        orderEntity.setTotalAmount(total);
        // Set total payable amount (total payable amount = order total amount + shipping fee)
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        // Set promotion discount amount
        orderEntity.setPromotionAmount(promotion);
        // Set coupon discount amount
        orderEntity.setCouponAmount(coupon);
        // Set integration discount amount
        orderEntity.setIntegrationAmount(integration);
        // Set integration points
        orderEntity.setIntegration(gift.intValue());
        // Set growth value
        orderEntity.setGrowth(growth.intValue());
    }


    // 保存 订单 & 订单项
    private void saveOrder(OrderCreateTo order) {
        // 保存订单
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        // 保存订单项
        List<OrderItemEntity> orderItems = order.getItems();
        for (OrderItemEntity orderItem : orderItems) {
            orderItemService.save(orderItem);
        }
    }

    // Encapsulate OrderConfirmVo object
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();

        // Get loginMember from ThreadLocal
        MemberResponseVO loginMember = LoginUserInterceptor.loginUser.get();

        /*
         * Issue with Feign remote call losing request context during asynchronous requests
         * Reason:
         * - Feign remote call request is made by a sub-thread (asynchronous task)
         * - Only the main thread can get the original request context, sub-threads cannot access data in the main thread, hence losing the original request context
         * - Due to the sub-thread (asynchronous task) not having the original request context,
         * - Feign remote call loses the original request context information when made by a sub-thread.
         *
         * Consequence:
         * - Feign remote call to other microservices, which cannot retrieve original request data (e.g., Cookie headers),
         * - The remote microservice cannot get the original request Cookie data, thus cannot determine if the user is logged in,
         * - This leads to assuming the user is not logged in, causing logical errors and further issues.
         *
         * Solution:
         * - Step 1: Get the original request context from the main thread,
         * - Step 2: Save the original request context to the sub-thread executing the asynchronous task (sub-thread sharing the main thread's request context).
         */

        // Get the original request context from the main thread
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // Start asynchronous task: Get member shipping address
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            // Save the original request context to the sub-thread executing the asynchronous task
            RequestContextHolder.setRequestAttributes(requestAttributes);

            // Get member shipping address
            List<MemberAddressVo> address = memberFeignService.getAddressByMemberId(loginMember.getId());
            confirmVo.setAddress(address);
        }, executor);

        // Start asynchronous task: Get all selected shopping items & check if each item is in stock
        CompletableFuture<Void> cartItemsFuture = CompletableFuture.runAsync(() -> {
            // Save the original request context to the sub-thread executing the asynchronous task
            RequestContextHolder.setRequestAttributes(requestAttributes);

            // Get all selected shopping items for the current user
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(items);
        }, executor).thenRunAsync(() -> {
            // The current asynchronous task will execute after the previous one completes - Serializing asynchronous tasks

            // All selected shopping items
            List<OrderItemVo> items = confirmVo.getItems();
            // All skuId collections corresponding to shopping items
            List<Long> skuIdList = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            // Batch query to check if items are in stock
            R skuStockVoR = wareFeignService.getSkuHasStock(skuIdList);
            // Get SkuStockVo collection from skuStockVoR
            // Each SkuStockVo contains item id & stock status
            List<SkuStockVo> skuStockVoList = skuStockVoR.getData(new TypeReference<List<SkuStockVo>>() {
            });
            if (skuStockVoList != null) {
                // Map<skuId, hasStock>
                Map<Long, Boolean> map = skuStockVoList.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(map);
            }
        }, executor);

        // Set coupon information (member points)
        Integer integration = loginMember.getIntegration();
        confirmVo.setIntegration(integration);

        // Create an anti-duplicate token
        String token = UUID.randomUUID().toString().replace("-", "");
        // Save an anti-duplicate token in Redis
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + loginMember.getId(), token, 30, TimeUnit.MINUTES);
        // Save an anti-duplicate token in the OrderConfirmVo object
        // The server will pass the OrderConfirmVo object to the HTML page -> The page can get the anti-duplicate token
        confirmVo.setOrderToken(token);

        // Wait for all asynchronous tasks to complete
        CompletableFuture.allOf(addressFuture, cartItemsFuture).get();

        return confirmVo;
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }
}
