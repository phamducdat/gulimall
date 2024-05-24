package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * SPU Attribute Value
 *
 * @Author Zhengyuli
 * @Email zli78122@usc.edu
 * @Date 2020-06-22 23:03:41
 */
@Data
@TableName("pms_product_attr_value")
public class ProductAttrValueEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * SPU ID
     */
    private Long spuId;
    /**
     * Attribute ID
     */
    private Long attrId;
    /**
     * Attribute Name
     */
    private String attrName;
    /**
     * Attribute Value
     */
    private String attrValue;
    /**
     * Sort Order
     */
    private Integer attrSort;
    /**
     * Quick Display [0 - No, 1 - Yes]
     */
    private Integer quickShow;
}

