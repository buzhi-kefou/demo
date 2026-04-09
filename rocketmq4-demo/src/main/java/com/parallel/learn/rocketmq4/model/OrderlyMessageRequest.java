package com.parallel.learn.rocketmq4.model;

public class OrderlyMessageRequest extends DemoMessageRequest {

    private String shardingKey;

    public String getShardingKey() {
        return shardingKey;
    }

    public void setShardingKey(String shardingKey) {
        this.shardingKey = shardingKey;
    }
}
