package com.atguigu.gulimall.order.listener;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RabbitListener(queues = "order.release.order.queue")
@Component
public class OrderCloseListener {

    @Autowired
    private OrderService orderService;

    /**
     * Listener for the "order.release.order.queue" queue.
     * Upon receiving a message (order created and has been pending for 1 minute),
     * it checks if the order status is "Pending Payment". If it is, the order is canceled.
     */
    @RabbitHandler
    public void listener(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        System.out.println("Order Number [" + orderEntity.getOrderSn() + "]: " +
                "Order created and has been pending for 1 minute, checking if the status is 'Pending Payment'. If it is, the order will be canceled.");
        try {
            // Check if the order status is "Pending Payment". If it is, cancel the order.
            orderService.closeOrder(orderEntity);
            // Acknowledge the message
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // Reject the message -> Message will be requeued
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}
