package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * Attribute Group
 *
 * @Author Zhengyuli
 * @Email zli78122@usc.edu
 * @Date 2020-06-22 23:03:41
 */
@Data
@TableName("pms_attr_group")
public class AttrGroupEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Group ID
     */
    @TableId
    private Long attrGroupId;
    /**
     * Group Name
     */
    private String attrGroupName;
    /**
     * Sorting
     */
    private Integer sort;
    /**
     * Description
     */
    private String descript;
    /**
     * Group Icon
     */
    private String icon;
    /**
     * Category ID
     */
    private Long catelogId;

    // catelogPath attribute does not exist in the database table, it represents the path from ancestor nodes to itself
    @TableField(exist = false)
    private Long[] catelogPath;
}
