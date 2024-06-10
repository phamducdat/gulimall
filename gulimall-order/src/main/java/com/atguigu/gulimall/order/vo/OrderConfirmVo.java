package com.atguigu.gulimall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Order Confirmation Page VO
 */
public class OrderConfirmVo {

    // Member shipping address
    @Setter
    @Getter
    private List<MemberAddressVo> address;

    // All selected shopping items
    @Setter
    @Getter
    private List<OrderItemVo> items;

    // Invoice information
    // ...

    // Coupon information (member points)
    @Setter
    @Getter
    private Integer integration;

    // Product stock status (whether the product is in stock)
    @Setter
    @Getter
    private Map<Long, Boolean> stocks;

    // Order anti-duplicate token, prevents duplicate order submissions
    @Setter
    @Getter
    private String orderToken;

    // Total number of items (sum of all shopping items)
    private Integer count;

    public Integer getCount() {
        Integer i = 0;
        if (items != null) {
            for (OrderItemVo item : items) {
                i += item.getCount();
            }
        }
        return i;
    }

    // Total order amount
    private BigDecimal total;

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    // Amount payable
    private BigDecimal payPrice;

    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
