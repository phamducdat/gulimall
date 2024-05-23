package com.atguigu.gulimall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * Product Inventory
 * <p>
 * Represents the inventory details of a product SKU in a warehouse.
 *
 * @autor zhengyuli
 * @date 2020-06-23 00:25:27
 */
@Data
@TableName("wms_ware_sku")
//@TableName("stock_order_task")
public class WareSkuEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId
    private Long id;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * Warehouse ID
     */
    private Long wareId;

    /**
     * Stock quantity
     */
    private Integer stock;

    /**
     * SKU name
     */
    private String skuName;

    /**
     * Locked stock quantity
     */
    private Integer stockLocked;
}
