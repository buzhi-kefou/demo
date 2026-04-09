package com.parallel.learn.rocketmq4.consumer;

import com.parallel.learn.rocketmq4.config.DemoRocketMqProperties;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "demo.rocketmq", name = "consumers-enabled", havingValue = "true")
@RocketMQMessageListener(
        topic = "${demo.rocketmq.orderly-topic}",
        consumerGroup = "demo-orderly-consumer",
        selectorExpression = "*",
        consumeMode = ConsumeMode.ORDERLY,
        messageModel = MessageModel.CLUSTERING
)
public class OrderlyMessageConsumer implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(OrderlyMessageConsumer.class);

    private final DemoRocketMqProperties properties;

    public OrderlyMessageConsumer(DemoRocketMqProperties properties) {
        this.properties = properties;
    }

    @Override
    public void onMessage(String message) {
        log.info("Received orderly message. topic={}, body={}", properties.getOrderlyTopic(), message);
    }
}
