package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.constant.WareConstant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.service.PurchaseService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.MergeVo;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import com.atguigu.gulimall.ware.vo.PurchaseItemDoneVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService detailService;

    @Autowired
    private WareSkuService wareSkuService;

    // Complete Purchase
    @Transactional
    @Override
    public void done(PurchaseDoneVo doneVo) {
        // Purchase Order ID
        Long id = doneVo.getId();

        // Modify the status of the purchase order and purchase items.
        // If any purchase item's status is "failed", the purchase order's status will be "failed".

        // Flag to mark if the purchase order status is failed
        boolean flag = true;
        // List of purchase items
        List<PurchaseItemDoneVo> purchaseItems = doneVo.getItems();
        List<PurchaseDetailEntity> purchaseDetailEntities = new ArrayList<>();
        for (PurchaseItemDoneVo item : purchaseItems) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            // If any purchase item's status is "failed", the purchase order's status will be "failed"
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag = false;
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode());
            } else {
                // Current purchase item succeeded, set the status to "completed"
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                // Get the purchase item object by its ID
                PurchaseDetailEntity entity = detailService.getById(item.getItemId());
                // Add the successfully purchased items to the stock in the corresponding wms_ware_sku table
                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
            }
            detailEntity.setId(item.getItemId());
            purchaseDetailEntities.add(detailEntity);
        }
        // Batch save the purchase item data
        detailService.updateBatchById(purchaseDetailEntities);

        // Save the purchase order data
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }


    // Receive Purchase Order
    @Transactional
    @Override
    public void received(List<Long> ids) {
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            // Get the purchase order object by its ID
            PurchaseEntity purchaseEntity = this.getById(id);
            return purchaseEntity;
        }).filter(item -> {
            // Ensure the current purchase order status is "created" or "assigned"; otherwise, it cannot be received
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(item -> {
            // Change the purchase order status to "received"
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        // Batch update purchase order data
        this.updateBatchById(collect);

        // Update purchase item data
        collect.forEach((item) -> {
            List<PurchaseDetailEntity> entities = detailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> detailEntities = entities.stream().map(entity -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(entity.getId());
                // Change the purchase item status to "purchasing"
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            // Batch update purchase item data
            detailService.updateBatchById(detailEntities);
        });
    }


    // Merge Purchase Orders and Items - Add multiple purchase items to one purchase order
    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        // If purchase order ID is null, create and save a new purchase order object
        // If purchase order ID is not null, only merge purchase orders and items if the purchase order status is 0 or 1
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            // Get purchase order ID
            purchaseId = purchaseEntity.getId();
        } else {
            PurchaseEntity purchaseEntity = this.getById(purchaseId);
            if (purchaseEntity.getStatus() != 0 && purchaseEntity.getStatus() != 1) {
                return;
            }
        }

        // Save purchase items
        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(item -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(item);
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return detailEntity;
        }).collect(Collectors.toList());
        detailService.updateBatchById(collect);

        // Update purchase order
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }


    // Query unreceived purchase orders
    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }
}
