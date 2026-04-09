package com.parallel.learn.rocketmq4.model;

import javax.validation.constraints.NotBlank;

public class BatchMessageItem {

    private String key;
    private String tag;

    @NotBlank
    private String body;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
