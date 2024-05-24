package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Product Category (Level 3)
 *
 * @Author Zhengyuli
 * @Email zli78122@usc.edu
 * @Date 2020-06-22 23:03:41
 */
@Data
@TableName("pms_category")
public class CategoryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Category ID
     */
    @TableId
    private Long catId;
    /**
     * Category Name
     */
    private String name;
    /**
     * Parent Category ID
     */
    private Long parentCid;
    /**
     * Level
     */
    private Integer catLevel;
    /**
     * Display Status [0 - Not Displayed, 1 - Displayed]
     *
     * @TableLogic Logical Deletion, indicates that showStatus is a logical deletion field
     * value = "1"  : When showStatus=1, it means the current record is not deleted
     * delval = "0" : When showStatus=0, it means the current record is logically deleted
     */
    @TableLogic(value = "1", delval = "0")
    private Integer showStatus;
    /**
     * Sort Order
     */
    private Integer sort;
    /**
     * Icon URL
     */
    private String icon;
    /**
     * Measurement Unit
     */
    private String productUnit;
    /**
     * Product Count
     */
    private Integer productCount;

    // The children attribute does not exist in the database table
    @TableField(exist = false)
    // If children is empty, the object will not include the children attribute
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CategoryEntity> children;
}
