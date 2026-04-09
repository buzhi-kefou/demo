package com.parallel.learn.rocketmq4.service;

import com.parallel.learn.rocketmq4.config.DemoRocketMqProperties;
import com.parallel.learn.rocketmq4.model.BatchMessageItem;
import com.parallel.learn.rocketmq4.model.BatchMessageRequest;
import com.parallel.learn.rocketmq4.model.DelayMessageRequest;
import com.parallel.learn.rocketmq4.model.OrderlyMessageRequest;
import com.parallel.learn.rocketmq4.model.TransactionMessageRequest;
import com.parallel.learn.rocketmq4.service.impl.RocketMqDemoMessageService;
import java.util.List;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RocketMqDemoMessageServiceTest {

    private RocketMQTemplate rocketMQTemplate;
    private RocketMqDemoMessageService service;


    @BeforeEach
    void setUp() {
        rocketMQTemplate = mock(RocketMQTemplate.class);
        service = new RocketMqDemoMessageService(rocketMQTemplate, new DemoRocketMqProperties());
    }

    @Test
    void shouldSendOrderlyMessageWithDefaultShardingKey() {
        when(rocketMQTemplate.syncSendOrderly(anyString(), anyMessage(), anyString())).thenReturn(sendResult("orderly-1"));

        OrderlyMessageRequest request = new OrderlyMessageRequest();
        request.setBody("order-body");

        service.sendOrderly(request);

        ArgumentCaptor<String> shardingCaptor = ArgumentCaptor.forClass(String.class);
        verify(rocketMQTemplate).syncSendOrderly(anyString(), anyMessage(), shardingCaptor.capture());
        assertThat(shardingCaptor.getValue()).isEqualTo("default-order-group");
    }

    @Test
    void shouldSendDelayMessageWithConfiguredLevel() {
        when(rocketMQTemplate.syncSend(anyString(), any(), anyLong(), anyInt())).thenReturn(sendResult("delay-1"));

        DelayMessageRequest request = new DelayMessageRequest();
        request.setBody("delay-body");
        request.setDelayLevel(4);

        assertThat(service.sendDelay(request).getStatus()).isEqualTo("SEND_OK");
        verify(rocketMQTemplate).syncSend(anyString(), any(), anyLong(), eq(4));
    }

    @Test
    void shouldSendBatchMessages() {
        when(rocketMQTemplate.syncSend(anyString(), any(List.class))).thenReturn(sendResult("batch-1"));

        BatchMessageItem item = new BatchMessageItem();
        item.setBody("batch-body");
        BatchMessageRequest request = new BatchMessageRequest();
        request.setMessages(List.of(item));

        assertThat(service.sendBatch(request).getMessageId()).isEqualTo("batch-1");
    }

    @Test
    void shouldSendTransactionMessage() {
        when(rocketMQTemplate.sendMessageInTransaction(anyString(), any(), any()))
                .thenReturn(transactionSendResult("tx-1"));

        TransactionMessageRequest request = new TransactionMessageRequest();
        request.setBody("tx-body");
        request.setBusinessId("commit-order-1001");

        assertThat(service.sendTransaction(request).getStatus()).isEqualTo("SEND_OK");
        verify(rocketMQTemplate).sendMessageInTransaction(anyString(), any(), eq("commit-order-1001"));
    }

    private SendResult sendResult(String messageId) {
        SendResult result = new SendResult();
        result.setSendStatus(SendStatus.SEND_OK);
        result.setMsgId(messageId);
        return result;
    }

    private TransactionSendResult transactionSendResult(String messageId) {
        TransactionSendResult result = new TransactionSendResult();
        result.setSendStatus(SendStatus.SEND_OK);
        result.setMsgId(messageId);
        return result;
    }

    private Message<?> anyMessage() {
        return org.mockito.ArgumentMatchers.any(Message.class);
    }
}
