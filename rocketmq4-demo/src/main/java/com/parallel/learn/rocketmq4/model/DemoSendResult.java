package com.parallel.learn.rocketmq4.model;

public class DemoSendResult {

    private final boolean success;
    private final String messageId;
    private final String status;
    private final String detail;

    private DemoSendResult(boolean success, String messageId, String status, String detail) {
        this.success = success;
        this.messageId = messageId;
        this.status = status;
        this.detail = detail;
    }

    public static DemoSendResult success(String messageId, String status, String detail) {
        return new DemoSendResult(true, messageId, status, detail);
    }

    public static DemoSendResult failure(String detail) {
        return new DemoSendResult(false, null, "FAILED", detail);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }
}
