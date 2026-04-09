package com.parallel.learn.rocketmq4.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "demo.rocketmq")
public class DemoRocketMqProperties {

    private String normalTopic = "demo-normal-topic";
    private String orderlyTopic = "demo-orderly-topic";
    private String delayTopic = "demo-delay-topic";
    private String batchTopic = "demo-batch-topic";
    private String filterTopic = "demo-filter-topic";
    private String transactionTopic = "demo-transaction-topic";
    private String oneWayTopic = "demo-one-way-topic";
    private String defaultTag = "demo";
    private String filterTag = "blue";
    private boolean consumersEnabled = false;

    public String getNormalTopic() {
        return normalTopic;
    }

    public void setNormalTopic(String normalTopic) {
        this.normalTopic = normalTopic;
    }

    public String getOrderlyTopic() {
        return orderlyTopic;
    }

    public void setOrderlyTopic(String orderlyTopic) {
        this.orderlyTopic = orderlyTopic;
    }

    public String getDelayTopic() {
        return delayTopic;
    }

    public void setDelayTopic(String delayTopic) {
        this.delayTopic = delayTopic;
    }

    public String getBatchTopic() {
        return batchTopic;
    }

    public void setBatchTopic(String batchTopic) {
        this.batchTopic = batchTopic;
    }

    public String getFilterTopic() {
        return filterTopic;
    }

    public void setFilterTopic(String filterTopic) {
        this.filterTopic = filterTopic;
    }

    public String getTransactionTopic() {
        return transactionTopic;
    }

    public void setTransactionTopic(String transactionTopic) {
        this.transactionTopic = transactionTopic;
    }

    public String getOneWayTopic() {
        return oneWayTopic;
    }

    public void setOneWayTopic(String oneWayTopic) {
        this.oneWayTopic = oneWayTopic;
    }

    public String getDefaultTag() {
        return defaultTag;
    }

    public void setDefaultTag(String defaultTag) {
        this.defaultTag = defaultTag;
    }

    public String getFilterTag() {
        return filterTag;
    }

    public void setFilterTag(String filterTag) {
        this.filterTag = filterTag;
    }

    public boolean isConsumersEnabled() {
        return consumersEnabled;
    }

    public void setConsumersEnabled(boolean consumersEnabled) {
        this.consumersEnabled = consumersEnabled;
    }
}
