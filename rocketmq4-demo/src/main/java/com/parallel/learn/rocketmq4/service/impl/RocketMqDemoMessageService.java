package com.parallel.learn.rocketmq4.service.impl;

import com.parallel.learn.rocketmq4.config.DemoRocketMqProperties;
import com.parallel.learn.rocketmq4.model.BatchMessageItem;
import com.parallel.learn.rocketmq4.model.BatchMessageRequest;
import com.parallel.learn.rocketmq4.model.DelayMessageRequest;
import com.parallel.learn.rocketmq4.model.DemoMessageRequest;
import com.parallel.learn.rocketmq4.model.DemoSendResult;
import com.parallel.learn.rocketmq4.model.OrderlyMessageRequest;
import com.parallel.learn.rocketmq4.model.TransactionMessageRequest;
import com.parallel.learn.rocketmq4.service.DemoMessageService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RocketMqDemoMessageService implements DemoMessageService {

    private static final Logger log = LoggerFactory.getLogger(RocketMqDemoMessageService.class);

    private final RocketMQTemplate rocketMQTemplate;
    private final DemoRocketMqProperties properties;

    public RocketMqDemoMessageService(RocketMQTemplate rocketMQTemplate, DemoRocketMqProperties properties) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.properties = properties;
    }

    @Override
    public DemoSendResult sendNormal(DemoMessageRequest request) {
        SendResult result = rocketMQTemplate.syncSend(
                destination(resolveTopic(request.getTopic(), properties.getNormalTopic()),
                        resolveTag(request.getTag(), properties.getDefaultTag())),
                buildMessage(request.getBody(), request.getKey()));
        return mapResult(result, "Normal message sent");
    }

    @Override
    public DemoSendResult sendOrderly(OrderlyMessageRequest request) {
        String shardingKey = StringUtils.hasText(request.getShardingKey()) ? request.getShardingKey() : "default-order-group";
        SendResult result = rocketMQTemplate.syncSendOrderly(
                destination(resolveTopic(request.getTopic(), properties.getOrderlyTopic()),
                        resolveTag(request.getTag(), properties.getDefaultTag())),
                buildMessage(request.getBody(), request.getKey()),
                shardingKey);
        return mapResult(result, "Orderly message sent with shardingKey=" + shardingKey);
    }

    @Override
    public DemoSendResult sendDelay(DelayMessageRequest request) {
        int delayLevel = request.getDelayLevel() == null ? 3 : request.getDelayLevel();
        SendResult result = rocketMQTemplate.syncSend(
                destination(resolveTopic(request.getTopic(), properties.getDelayTopic()),
                        resolveTag(request.getTag(), properties.getDefaultTag())),
                buildMessage(request.getBody(), request.getKey()),
                3000,
                delayLevel);
        return mapResult(result, "Delay message sent with delayLevel=" + delayLevel);
    }

    @Override
    public DemoSendResult sendBatch(BatchMessageRequest request) {
        List<Message<String>> messages = request.getMessages().stream()
                .map(this::buildBatchMessage)
                .collect(Collectors.toList());
        SendResult result = rocketMQTemplate.syncSend(resolveTopic(request.getTopic(), properties.getBatchTopic()), messages);
        return mapResult(result, "Batch message count=" + messages.size());
    }

    @Override
    public DemoSendResult sendFiltered(DemoMessageRequest request) {
        String tag = resolveTag(request.getTag(), properties.getFilterTag());
        SendResult result = rocketMQTemplate.syncSend(
                destination(resolveTopic(request.getTopic(), properties.getFilterTopic()), tag),
                buildMessage(request.getBody(), request.getKey()));
        return mapResult(result, "Filter message sent with tag=" + tag);
    }

    @Override
    public DemoSendResult sendTransaction(TransactionMessageRequest request) {
        String businessId = StringUtils.hasText(request.getBusinessId()) ? request.getBusinessId() : "biz-" + UUID.randomUUID();
        Message<String> message = MessageBuilder.withPayload(request.getBody())
                .setHeader(RocketMQHeaders.KEYS, resolveKey(request.getKey()))
                .setHeader("businessId", businessId)
                .build();
        SendResult result = rocketMQTemplate.sendMessageInTransaction(
                resolveTopic(request.getTopic(), properties.getTransactionTopic()),
                message,
                businessId);
        return mapResult(result, "Transaction message sent with businessId=" + businessId);
    }

    @Override
    public DemoSendResult sendOneWay(DemoMessageRequest request) {
        String topic = resolveTopic(request.getTopic(), properties.getOneWayTopic());
        String tag = resolveTag(request.getTag(), properties.getDefaultTag());
        String key = resolveKey(request.getKey());
        rocketMQTemplate.sendOneWay(destination(topic, tag), buildMessage(request.getBody(), key));
        log.info("Sent one-way message. topic={}, tag={}, key={}", topic, tag, key);
        return DemoSendResult.success(null, "SENT", "One-way message sent without broker ack");
    }

    private DemoSendResult mapResult(SendResult result, String detail) {
        return DemoSendResult.success(result.getMsgId(), result.getSendStatus().name(), detail);
    }

    private Message<String> buildMessage(String body, String key) {
        return MessageBuilder.withPayload(body)
                .setHeader(RocketMQHeaders.KEYS, resolveKey(key))
                .build();
    }

    private Message<String> buildBatchMessage(BatchMessageItem item) {
        MessageBuilder<String> builder = MessageBuilder.withPayload(item.getBody())
                .setHeader(RocketMQHeaders.KEYS, resolveKey(item.getKey()));
        if (StringUtils.hasText(item.getTag())) {
            builder.setHeader(RocketMQHeaders.TAGS, item.getTag());
        }
        return builder.build();
    }

    private String destination(String topic, String tag) {
        return topic + ":" + tag;
    }

    private String resolveTopic(String requestedTopic, String defaultTopic) {
        return StringUtils.hasText(requestedTopic) ? requestedTopic : defaultTopic;
    }

    private String resolveTag(String requestedTag, String defaultTag) {
        return StringUtils.hasText(requestedTag) ? requestedTag : defaultTag;
    }

    private String resolveKey(String key) {
        return StringUtils.hasText(key) ? key : UUID.randomUUID().toString();
    }
}
