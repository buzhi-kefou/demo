package com.parallel.learn.rocketmq4.model;

import javax.validation.constraints.NotBlank;

public class DemoMessageRequest {

    private String topic;
    private String tag;
    private String key;

    @NotBlank
    private String body;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
