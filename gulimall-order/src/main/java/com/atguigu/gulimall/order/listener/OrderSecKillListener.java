package com.atguigu.gulimall.order.listener;

import com.atguigu.common.to.mq.SecKillOrderTo;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RabbitListener(queues = "order.seckill.order.queue")
public class OrderSecKillListener {

    @Autowired
    private OrderService orderService;

    /**
     * Listener for the "order.seckill.order.queue" queue.
     * Upon receiving a message (SecKill successful), it creates a SecKill order.
     */
    @RabbitHandler
    public void listener(SecKillOrderTo secKillOrderTo, Channel channel, Message message) throws IOException {
        System.out.println("Order Number [" + secKillOrderTo.getOrderSn() + "]: SecKill successful, creating SecKill order.");
        try {
            // Create SecKill order
            orderService.createSecKillOrder(secKillOrderTo);
            // Acknowledge the message
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // Reject the message -> Message will be requeued
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
