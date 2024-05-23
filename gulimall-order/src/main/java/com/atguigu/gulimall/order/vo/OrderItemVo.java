package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物项VO
 */
@Data
public class OrderItemVo {

    private Long skuId;

    private String title;

    private String image;

    // Sales attributes
    private List<String> skuAttr;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;

    // commodity weight
    private BigDecimal weight;
}
