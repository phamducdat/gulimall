package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * SKU Information
 *
 * @Author Zhengyuli
 * @Email zli78122@usc.edu
 * @Date 2020-06-22 23:03:41
 */
@Data
@TableName("pms_sku_info")
public class SkuInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    @TableId
    private Long skuId;
    /**
     * SPU ID
     */
    private Long spuId;
    /**
     * SKU Name
     */
    private String skuName;
    /**
     * SKU Description
     */
    private String skuDesc;
    /**
     * Category ID
     */
    private Long catalogId;
    /**
     * Brand ID
     */
    private Long brandId;
    /**
     * Default Image
     */
    private String skuDefaultImg;
    /**
     * Title
     */
    private String skuTitle;
    /**
     * Subtitle
     */
    private String skuSubtitle;
    /**
     * Price
     */
    private BigDecimal price;
    /**
     * Sales Count
     */
    private Long saleCount;
}
