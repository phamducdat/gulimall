package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * SKU Images
 *
 * @Author Zhengyuli
 * @Email zli78122@usc.edu
 * @Date 2020-06-22 23:03:41
 */
@Data
@TableName("pms_sku_images")
public class SkuImagesEntity implements Serializable {
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
     * Image URL
     */
    private String imgUrl;
    /**
     * Sort Order
     */
    private Integer imgSort;
    /**
     * Default Image [0 - Not Default, 1 - Default]
     */
    private Integer defaultImg;
}
