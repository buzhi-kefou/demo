package com.parallel.learn.rabbitmq.service;

import com.parallel.learn.rabbitmq.model.Message;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * 消息消费者服务
 */
@Service
public class MessageConsumerService {

    /**
     * 监听Direct队列
     */
    @RabbitListener(queues = "${demo.rabbitmq.queues.direct}")
    public void receiveDirectMessage(Message message, Channel channel, org.springframework.amqp.core.Message amqpMessage) {
        try {
            System.out.println("接收到Direct消息: " + message);
            // 手动确认消息
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            System.err.println("处理Direct消息异常: " + e.getMessage());
            try {
                // 拒绝消息，不重新入队
                channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, false);
            } catch (Exception ex) {
                System.err.println("拒绝消息异常: " + ex.getMessage());
            }
        }
    }

    /**
     * 监听Fanout队列
     */
    @RabbitListener(queues = "${demo.rabbitmq.queues.fanout}")
    public void receiveFanoutMessage(Message message, Channel channel, org.springframework.amqp.core.Message amqpMessage) {
        try {
            System.out.println("接收到Fanout消息: " + message);
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            System.err.println("处理Fanout消息异常: " + e.getMessage());
            try {
                channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, false);
            } catch (Exception ex) {
                System.err.println("拒绝消息异常: " + ex.getMessage());
            }
        }
    }

    /**
     * 监听Topic队列
     */
    @RabbitListener(queues = "${demo.rabbitmq.queues.topic}")
    public void receiveTopicMessage(Message message, Channel channel, org.springframework.amqp.core.Message amqpMessage) {
        try {
            System.out.println("接收到Topic消息: " + message);
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            System.err.println("处理Topic消息异常: " + e.getMessage());
            try {
                channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, false);
            } catch (Exception ex) {
                System.err.println("拒绝消息异常: " + ex.getMessage());
            }
        }
    }

    /**
     * 监听Headers队列
     */
    @RabbitListener(queues = "${demo.rabbitmq.queues.headers}")
    public void receiveHeadersMessage(Message message, Channel channel, org.springframework.amqp.core.Message amqpMessage) {
        try {
            System.out.println("接收到Headers消息: " + message);
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            System.err.println("处理Headers消息异常: " + e.getMessage());
            try {
                channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, false);
            } catch (Exception ex) {
                System.err.println("拒绝消息异常: " + ex.getMessage());
            }
        }
    }

    /**
     * 监听延迟队列
     */
    @RabbitListener(queues = "${demo.rabbitmq.queues.delay}")
    public void receiveDelayMessage(Message message, Channel channel, org.springframework.amqp.core.Message amqpMessage) {
        try {
            System.out.println("接收到延迟消息: " + message);
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            System.err.println("处理延迟消息异常: " + e.getMessage());
            try {
                channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, false);
            } catch (Exception ex) {
                System.err.println("拒绝消息异常: " + ex.getMessage());
            }
        }
    }

    /**
     * 监听死信队列
     */
    @RabbitListener(queues = "${demo.rabbitmq.queues.dlq}")
    public void receiveDeadLetterMessage(Message message, Channel channel, org.springframework.amqp.core.Message amqpMessage) {
        try {
            System.out.println("接收到死信队列消息: " + message);
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            System.err.println("处理死信队列消息异常: " + e.getMessage());
            try {
                channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, false);
            } catch (Exception ex) {
                System.err.println("拒绝消息异常: " + ex.getMessage());
            }
        }
    }
}
