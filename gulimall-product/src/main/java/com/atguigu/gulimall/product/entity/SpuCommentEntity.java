package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Product Comment
 *
 * @Author Zhengyuli
 * @Email zli78122@usc.edu
 * @Date 2020-06-22 23:03:41
 */
@Data
@TableName("pms_spu_comment")
public class SpuCommentEntity implements Serializable {
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
     * SPU ID
     */
    private Long spuId;
    /**
     * Product Name
     */
    private String spuName;
    /**
     * Member Nickname
     */
    private String memberNickName;
    /**
     * Star Rating
     */
    private Integer star;
    /**
     * Member IP
     */
    private String memberIp;
    /**
     * Creation Time
     */
    private Date createTime;
    /**
     * Display Status [0 - Not Displayed, 1 - Displayed]
     */
    private Integer showStatus;
    /**
     * Attribute Combination at Purchase
     */
    private String spuAttributes;
    /**
     * Likes Count
     */
    private Integer likesCount;
    /**
     * Reply Count
     */
    private Integer replyCount;
    /**
     * Comment Resources [JSON data; [{type: file type, url: resource path}]]
     */
    private String resources;
    /**
     * Content
     */
    private String content;
    /**
     * Member Icon
     */
    private String memberIcon;
    /**
     * Comment Type [0 - Direct comment on product, 1 - Reply to comment]
     */
    private Integer commentType;
}
