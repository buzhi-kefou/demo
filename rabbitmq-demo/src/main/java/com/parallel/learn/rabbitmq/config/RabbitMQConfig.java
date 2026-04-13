package com.parallel.learn.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 * 配置交换机、队列和绑定关系
 */
@Configuration
public class RabbitMQConfig {

    @Value("${demo.rabbitmq.exchanges.direct}")
    private String directExchange;

    @Value("${demo.rabbitmq.exchanges.fanout}")
    private String fanoutExchange;

    @Value("${demo.rabbitmq.exchanges.topic}")
    private String topicExchange;

    @Value("${demo.rabbitmq.exchanges.headers}")
    private String headersExchange;

    @Value("${demo.rabbitmq.queues.direct}")
    private String directQueue;

    @Value("${demo.rabbitmq.queues.fanout}")
    private String fanoutQueue;

    @Value("${demo.rabbitmq.queues.topic}")
    private String topicQueue;

    @Value("${demo.rabbitmq.queues.headers}")
    private String headersQueue;

    @Value("${demo.rabbitmq.queues.dlq}")
    private String dlqQueue;

    @Value("${demo.rabbitmq.routing-keys.direct}")
    private String directRoutingKey;

    @Value("${demo.rabbitmq.routing-keys.topic}")
    private String topicRoutingKey;

    @Value("${demo.rabbitmq.queues.delay}")
    private String delayQueue;

    @Value("${demo.rabbitmq.queues.delay-exchange}")
    private String delayExchange;

    @Value("${demo.rabbitmq.queues.delay-routing-key}")
    private String delayRoutingKey;

    /**
     * JSON消息转换器
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置RabbitTemplate，使用JSON消息转换器
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // ==================== Direct Exchange ====================

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(directExchange, true, false);
    }

    @Bean
    public Queue directQueue() {
        return QueueBuilder.durable(directQueue).build();
    }

    @Bean
    public Binding directBinding() {
        return BindingBuilder.bind(directQueue())
                .to(directExchange())
                .with(directRoutingKey);
    }

    // ==================== Fanout Exchange ====================

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(fanoutExchange, true, false);
    }

    @Bean
    public Queue fanoutQueue() {
        return QueueBuilder.durable(fanoutQueue).build();
    }

    @Bean
    public Binding fanoutBinding() {
        return BindingBuilder.bind(fanoutQueue())
                .to(fanoutExchange());
    }

    // ==================== Topic Exchange ====================

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(topicExchange, true, false);
    }

    @Bean
    public Queue topicQueue() {
        return QueueBuilder.durable(topicQueue).build();
    }

    @Bean
    public Binding topicBinding() {
        return BindingBuilder.bind(topicQueue())
                .to(topicExchange())
                .with(topicRoutingKey);
    }

    // ==================== Headers Exchange ====================

    @Bean
    public HeadersExchange headersExchange() {
        return new HeadersExchange(headersExchange, true, false);
    }

    @Bean
    public Queue headersQueue() {
        return QueueBuilder.durable(headersQueue).build();
    }

    @Bean
    public Binding headersBinding() {
        return BindingBuilder.bind(headersQueue())
                .to(headersExchange())
                .whereAll("key1", "key2").exist();
    }

    // ==================== Dead Letter Queue ====================

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(dlqQueue).build();
    }

    // ==================== Delay Queue ====================

    @Bean
    public Queue delayQueue() {
        return QueueBuilder.durable(delayQueue)
                .withArgument("x-message-ttl", 60000) // 消息过期时间60秒
                .withArgument("x-dead-letter-exchange", directExchange) // 过期后转发到direct交换机
                .withArgument("x-dead-letter-routing-key", directRoutingKey) // 使用direct路由键
                .build();
    }

    @Bean
    public DirectExchange delayExchange() {
        return new DirectExchange(delayExchange, true, false);
    }

    @Bean
    public Binding delayBinding() {
        return BindingBuilder.bind(delayQueue())
                .to(delayExchange())
                .with(delayRoutingKey);
    }
}
