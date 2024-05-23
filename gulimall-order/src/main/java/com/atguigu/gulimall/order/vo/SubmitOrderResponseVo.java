package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * Submit Order Response Data VO
 */
@Data
public class SubmitOrderResponseVo {
    // Order
    private OrderEntity order;
    // Status code (0: Success)
    private Integer code;
}
