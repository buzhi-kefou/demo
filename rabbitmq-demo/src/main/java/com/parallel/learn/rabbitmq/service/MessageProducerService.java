package com.parallel.learn.rabbitmq.service;

import com.parallel.learn.rabbitmq.model.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 消息生产者服务
 */
@Service
public class MessageProducerService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${demo.rabbitmq.exchanges.direct}")
    private String directExchange;

    @Value("${demo.rabbitmq.exchanges.fanout}")
    private String fanoutExchange;

    @Value("${demo.rabbitmq.exchanges.topic}")
    private String topicExchange;

    @Value("${demo.rabbitmq.exchanges.headers}")
    private String headersExchange;

    @Value("${demo.rabbitmq.routing-keys.direct}")
    private String directRoutingKey;

    @Value("${demo.rabbitmq.routing-keys.topic}")
    private String topicRoutingKey;

    @Value("${demo.rabbitmq.queues.delay-exchange}")
    private String delayExchange;

    @Value("${demo.rabbitmq.queues.delay-routing-key}")
    private String delayRoutingKey;

    /**
     * 发送Direct类型消息
     */
    public void sendDirectMessage(String content) {
        Message message = new Message(UUID.randomUUID().toString(), content, "direct");
        rabbitTemplate.convertAndSend(directExchange, directRoutingKey, message);
        System.out.println("发送Direct消息: " + message);
    }

    /**
     * 发送Fanout类型消息
     */
    public void sendFanoutMessage(String content) {
        Message message = new Message(UUID.randomUUID().toString(), content, "fanout");
        rabbitTemplate.convertAndSend(fanoutExchange, "", message);
        System.out.println("发送Fanout消息: " + message);
    }

    /**
     * 发送Topic类型消息
     */
    public void sendTopicMessage(String content) {
        Message message = new Message(UUID.randomUUID().toString(), content, "topic");
        rabbitTemplate.convertAndSend(topicExchange, topicRoutingKey, message);
        System.out.println("发送Topic消息: " + message);
    }

    /**
     * 发送Headers类型消息
     */
    public void sendHeadersMessage(String content) {
        Message message = new Message(UUID.randomUUID().toString(), content, "headers");
        MessagePostProcessor messagePostProcessor = msg -> {
            Map<String, Object> headers = msg.getMessageProperties().getHeaders();
            headers.put("key1", "value1");
            headers.put("key2", "value2");
            return msg;
        };
        rabbitTemplate.convertAndSend(headersExchange, "", message, messagePostProcessor);
        System.out.println("发送Headers消息: " + message);
    }

    /**
     * 发送延迟消息
     */
    public void sendDelayMessage(String content, long delayMillis) {
        Message message = new Message(UUID.randomUUID().toString(), content, "delay");
        MessagePostProcessor messagePostProcessor = msg -> {
            msg.getMessageProperties().setDelay((int) delayMillis);
            return msg;
        };
        rabbitTemplate.convertAndSend(delayExchange, delayRoutingKey, message, messagePostProcessor);
        System.out.println("发送延迟消息: " + message + ", 延迟时间: " + delayMillis + "毫秒");
    }

    /**
     * 发送消息到死信队列
     */
    public void sendToDeadLetterQueue(String content) {
        Message message = new Message(UUID.randomUUID().toString(), content, "dlq");
        rabbitTemplate.convertAndSend(directExchange, directRoutingKey, message);
        System.out.println("发送消息到死信队列: " + message);
    }
}
