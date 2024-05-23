package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单提交的数据
 */
@Data
public class OrderSubmitVo {
    // Shipping address ID
    private Long addrId;

    // Payment method
    private Integer payType;

    // Anti-duplicate token
    private String orderToken;

    // Total amount payable (needs to be verified)
    private BigDecimal payPrice;

    // Order remarks
    private String note;

    // Discount information
    // ...

    // Invoice information
    // ...
}