package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * SPU Images
 *
 * @Author Zhengyuli
 * @Email zli78122@usc.edu
 * @Date 2020-06-22 23:03:42
 */
@Data
@TableName("pms_spu_images")
public class SpuImagesEntity implements Serializable {
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
     * Image Name
     */
    private String imgName;
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
