package com.parallel.learn.rocketmq4.service;

import com.parallel.learn.rocketmq4.model.BatchMessageRequest;
import com.parallel.learn.rocketmq4.model.DelayMessageRequest;
import com.parallel.learn.rocketmq4.model.DemoMessageRequest;
import com.parallel.learn.rocketmq4.model.DemoSendResult;
import com.parallel.learn.rocketmq4.model.OrderlyMessageRequest;
import com.parallel.learn.rocketmq4.model.TransactionMessageRequest;

public interface DemoMessageService {

    DemoSendResult sendNormal(DemoMessageRequest request);

    DemoSendResult sendOrderly(OrderlyMessageRequest request);

    DemoSendResult sendDelay(DelayMessageRequest request);

    DemoSendResult sendBatch(BatchMessageRequest request);

    DemoSendResult sendFiltered(DemoMessageRequest request);

    DemoSendResult sendTransaction(TransactionMessageRequest request);

    DemoSendResult sendOneWay(DemoMessageRequest request);
}
