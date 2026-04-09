package com.parallel.learn.rocketmq4.model;

public class DelayMessageRequest extends DemoMessageRequest {

    private Integer delayLevel = 3;

    public Integer getDelayLevel() {
        return delayLevel;
    }

    public void setDelayLevel(Integer delayLevel) {
        this.delayLevel = delayLevel;
    }
}
