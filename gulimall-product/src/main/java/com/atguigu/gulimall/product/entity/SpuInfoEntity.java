package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * SPU Information
 *
 * @Author Zhengyuli
 * @Email zli78122@usc.edu
 * @Date 2020-06-22 23:03:41
 */
@Data
@TableName("pms_spu_info")
public class SpuInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Product ID
     */
    @TableId
    private Long id;
    /**
     * Product Name
     */
    private String spuName;
    /**
     * Product Description
     */
    private String spuDescription;
    /**
     * Category ID
     */
    private Long catalogId;
    /**
     * Brand ID
     */
    private Long brandId;
    /**
     * Weight
     */
    private BigDecimal weight;
    /**
     * Publish Status [0 - Unpublished, 1 - Published]
     */
    private Integer publishStatus;
    /**
     * Creation Time
     */
    private Date createTime;
    /**
     * Update Time
     */
    private Date updateTime;
}
