package com.atguigu.gulimall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Purchase Detail
 * <p>
 * Represents the details of a purchase order.
 *
 * @autor zhengyuli
 * @date 2020-06-23 00:25:27
 */
@Data
@TableName("wms_purchase_detail")
public class PurchaseDetailEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId
    private Long id;

    /**
     * Purchase order ID
     */
    private Long purchaseId;

    /**
     * SKU ID of the purchased product
     */
    private Long skuId;

    /**
     * Quantity of the purchased product
     */
    private Integer skuNum;

    /**
     * Price of the purchased product
     */
    private BigDecimal skuPrice;

    /**
     * Warehouse ID
     */
    private Long wareId;

    /**
     * Status [0 - New, 1 - Assigned, 2 - Purchasing, 3 - Completed, 4 - Purchase Failed]
     */
    private Integer status;
}

