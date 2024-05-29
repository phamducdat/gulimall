package com.atguigu.gulimall.order.service;

import com.atguigu.common.to.mq.SecKillOrderTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Order Service
 * <p>
 * Author: Zhengyuli
 * Email: zli78122@usc.edu
 * Date: 2020-06-23 00:20:14
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    // Encapsulate OrderConfirmVo object
    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    // Submit an order (place an order)
    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    // Get order information by order number
    OrderEntity getOrderByOrderSn(String orderSn);

    // Check if the order status is "Pending Payment", if so, cancel the order
    void closeOrder(OrderEntity entity);

    // Get PayVo object by order number
    PayVo getOrderPay(String orderSn);

    // Paginated query of all orders for the currently logged-in user
    PageUtils queryPageWithItem(Map<String, Object> params);

    // After payment completion -> Save payment information & update order status
    String handPayResult(PayAsyncVo vo);

    // Create a flash sale order
    void createSecKillOrder(SecKillOrderTo secKillOrderTo);
}

