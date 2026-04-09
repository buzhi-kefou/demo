package com.parallel.learn.rocketmq4.model;

public class TransactionMessageRequest extends DemoMessageRequest {

    private String businessId;

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }
}
