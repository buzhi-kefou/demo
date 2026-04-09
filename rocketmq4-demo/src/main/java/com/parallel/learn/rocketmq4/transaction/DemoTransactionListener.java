package com.parallel.learn.rocketmq4.transaction;

import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component("demoTransactionExecutor")
@RocketMQTransactionListener(rocketMQTemplateBeanName = "rocketMQTemplate")
public class DemoTransactionListener implements RocketMQLocalTransactionListener {

    private static final Logger log = LoggerFactory.getLogger(DemoTransactionListener.class);

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object argument) {
        String businessId = argument == null ? "unknown" : argument.toString();
        RocketMQLocalTransactionState state = resolveState(businessId);
        log.info("Execute local transaction. businessId={}, state={}", businessId, state);
        return state;
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        Object businessIdHeader = message.getHeaders().get("businessId");
        String businessId = businessIdHeader == null ? null : businessIdHeader.toString();
        if (businessId == null) {
            Object keyHeader = message.getHeaders().get("KEYS");
            businessId = keyHeader == null ? null : keyHeader.toString();
        }
        RocketMQLocalTransactionState state = resolveState(businessId);
        log.info("Check local transaction. businessId={}, state={}", businessId, state);
        return state;
    }

    RocketMQLocalTransactionState resolveState(String businessId) {
        if (businessId == null) {
            return RocketMQLocalTransactionState.UNKNOWN;
        }
        String normalized = businessId.toLowerCase();
        if (normalized.contains("commit") || normalized.contains("success")) {
            return RocketMQLocalTransactionState.COMMIT;
        }
        if (normalized.contains("rollback") || normalized.contains("fail")) {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
        return RocketMQLocalTransactionState.UNKNOWN;
    }
}
