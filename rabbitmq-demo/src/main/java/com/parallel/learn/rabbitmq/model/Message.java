package com.parallel.learn.rabbitmq.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息实体类
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String content;
    private LocalDateTime createTime;
    private String type;

    public Message() {
    }

    public Message(String id, String content, String type) {
        this.id = id;
        this.content = content;
        this.type = type;
        this.createTime = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + "'," +
                ", content='" + content + "'," +
                ", createTime=" + createTime + "," +
                ", type='" + type + "'," +
                '}';
    }
}
