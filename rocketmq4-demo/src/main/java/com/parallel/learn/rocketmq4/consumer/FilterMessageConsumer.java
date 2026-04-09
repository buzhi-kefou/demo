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
        topic = "${demo.rocketmq.filter-topic}",
        consumerGroup = "demo-filter-consumer",
        selectorExpression = "${demo.rocketmq.filter-tag}"
)
public class FilterMessageConsumer implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(FilterMessageConsumer.class);

    private final DemoRocketMqProperties properties;

    public FilterMessageConsumer(DemoRocketMqProperties properties) {
        this.properties = properties;
    }

    @Override
    public void onMessage(String message) {
        log.info("Received filter message. topic={}, tag={}, body={}",
                properties.getFilterTopic(), properties.getFilterTag(), message);
    }
}
