package com.atguigu.gulimall.ware.service;

import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.vo.SkuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * Product Inventory
 * <p>
 * Author: zhengyuli
 * Email: zli78122@usc.edu
 * Date: 2020-06-23 00:25:27
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    // Paginated conditional query
    PageUtils queryPage(Map<String, Object> params);

    // Add stock for successfully purchased products
    void addStock(Long skuId, Long wareId, Integer skuNum);

    // Check if SKU has stock
    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    // Lock stock (only successful if all order items are locked successfully; if any order item fails to lock, the lock is considered a failure)
    Boolean orderLockStock(WareSkuLockVo lockVo);

    // Unlock stock based on StockLockedTo object
    void unlockStock(StockLockedTo to);

    // Unlock stock based on OrderTo object
    void unlockStock(OrderTo orderTo);
}

