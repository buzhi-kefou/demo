package com.parallel.learn.rocketmq4.consumer;

import com.parallel.learn.rocketmq4.config.DemoRocketMqProperties;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "demo.rocketmq", name = "consumers-enabled", havingValue = "true")
@RocketMQMessageListener(
        topic = "${demo.rocketmq.delay-topic}",
        consumerGroup = "demo-delay-consumer"
)
public class DelayMessageConsumer implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(DelayMessageConsumer.class);

    private final DemoRocketMqProperties properties;

    public DelayMessageConsumer(DemoRocketMqProperties properties) {
        this.properties = properties;
    }

    @Override
    public void onMessage(String message) {
        log.info("Received delay message. topic={}, body={}", properties.getDelayTopic(), message);
    }
}
