package com.parallel.learn.rocketmq4.model;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

public class BatchMessageRequest {

    private String topic;

    @Valid
    @NotEmpty
    private List<BatchMessageItem> messages = new ArrayList<>();

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<BatchMessageItem> getMessages() {
        return messages;
    }

    public void setMessages(List<BatchMessageItem> messages) {
        this.messages = messages;
    }
}
