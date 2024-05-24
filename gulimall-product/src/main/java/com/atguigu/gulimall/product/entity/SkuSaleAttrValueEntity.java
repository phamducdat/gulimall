package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * SKU Sales Attribute & Value
 *
 * @Author Zhengyuli
 * @Email zli78122@usc.edu
 * @Date 2020-06-22 23:03:41
 */
@Data
@TableName("pms_sku_sale_attr_value")
public class SkuSaleAttrValueEntity implements Serializable {
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
     * Attribute ID
     */
    private Long attrId;
    /**
     * Sales Attribute Name
     */
    private String attrName;
    /**
     * Sales Attribute Value
     */
    private String attrValue;
    /**
     * Sort Order
     */
    private Integer attrSort;
}
