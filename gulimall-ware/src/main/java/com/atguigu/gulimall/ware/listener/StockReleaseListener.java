package com.atguigu.gulimall.ware.listener;

import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    @Autowired
    private WareSkuService wareSkuService;

    /**
     * Listener for the "stock.release.stock.queue" queue.
     * Upon receiving a message (stock locked for more than 2 minutes), it executes the "unlock inventory" business logic.
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        System.out.println("Stock Work Order Detail ID = " + stockLockedTo.getDetailTo().getId() +
                ". Stock locked for more than 2 minutes, executing 'unlock inventory' business logic.");

        try {
            // Unlock inventory based on the StockLockedTo object
            wareSkuService.unlockStock(stockLockedTo);
            // Acknowledge the message
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // Reject the message -> Message will be requeued
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }


    /**
     * Listener for the "stock.release.stock.queue" queue.
     * Upon receiving a message (order canceled), it executes the "unlock inventory" business logic.
     */
    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo orderTo, Message message, Channel channel) throws IOException {
        System.out.println("Order Number [" + orderTo.getOrderSn() + "]: Order has been canceled, executing 'unlock inventory' business logic.");
        try {
            // Unlock inventory based on the OrderTo object
            wareSkuService.unlockStock(orderTo);
            // Acknowledge the message
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // Reject the message -> Message will be requeued
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}
