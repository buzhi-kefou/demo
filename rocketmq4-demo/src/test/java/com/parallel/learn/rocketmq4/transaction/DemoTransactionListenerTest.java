package com.parallel.learn.rocketmq4.transaction;

import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class DemoTransactionListenerTest {

    private final DemoTransactionListener listener = new DemoTransactionListener();

    @Test
    void shouldCommitTransactionWhenBusinessIdContainsCommit() {
        Message<String> message = MessageBuilder.withPayload("body").build();

        RocketMQLocalTransactionState state = listener.executeLocalTransaction(message, "commit-demo");

        assertThat(state).isEqualTo(RocketMQLocalTransactionState.COMMIT);
    }

    @Test
    void shouldRollbackTransactionWhenBusinessIdContainsRollback() {
        Message<String> message = MessageBuilder.withPayload("body")
                .setHeader("businessId", "rollback-demo")
                .build();

        RocketMQLocalTransactionState state = listener.checkLocalTransaction(message);

        assertThat(state).isEqualTo(RocketMQLocalTransactionState.ROLLBACK);
    }

    @Test
    void shouldReturnUnknownWhenBusinessIdHasNoHint() {
        assertThat(listener.resolveState("waiting-demo")).isEqualTo(RocketMQLocalTransactionState.UNKNOWN);
    }
}
