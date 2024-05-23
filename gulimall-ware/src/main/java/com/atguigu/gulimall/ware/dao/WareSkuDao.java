package com.atguigu.gulimall.ware.dao;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Product Inventory
 * <p>
 * Author: zhengyuli
 * Email: zli78122@usc.edu
 * Date: 2020-06-23 00:25:27
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    // Update product inventory record
    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    // Query the total inventory of the current SKU
    Long getSkuStock(Long skuId);

    // Query which warehouses have stock for the product based on skuId
    List<Long> listWareIdHasSkuStock(@Param("skuId") Long skuId);

    // Lock stock (lock stock based on product id, warehouse id, and quantity of product to be locked)
    Long lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);

    // Unlock stock (unlock stock based on product id, warehouse id, and quantity of product to be unlocked)
    void unLockStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);
}
