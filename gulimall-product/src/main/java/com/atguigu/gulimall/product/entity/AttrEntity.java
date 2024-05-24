package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * Product Attribute
 *
 * @Author Zhengyuli
 * @Email zli78122@usc.edu
 * @Date 2020-06-22 23:03:41
 */
@Data
@TableName("pms_attr")
public class AttrEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Attribute ID
     */
    @TableId
    private Long attrId;
    /**
     * Attribute Name
     */
    private String attrName;
    /**
     * Search Requirement [0 - Not Required, 1 - Required]
     */
    private Integer searchType;
    /**
     * Value Type [0 - Single Value, 1 - Multiple Values]
     */
    private Integer valueType;
    /**
     * Attribute Icon
     */
    private String icon;
    /**
     * Optional Values List [Comma Separated]
     */
    private String valueSelect;
    /**
     * Attribute Type [0 - Sales Attribute, 1 - Basic Attribute, 2 - Both Sales and Basic Attribute]
     */
    private Integer attrType;
    /**
     * Enable Status [0 - Disabled, 1 - Enabled]
     */
    private Long enable;
    /**
     * Category ID
     */
    private Long catelogId;
    /**
     * Quick Display [Whether to display on introduction; 0 - No, 1 - Yes], can still be adjusted in SKU
     */
    private Integer showDesc;
}

