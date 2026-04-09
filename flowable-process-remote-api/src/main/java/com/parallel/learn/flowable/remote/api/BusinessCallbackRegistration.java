package com.parallel.learn.flowable.remote.api;

public class BusinessCallbackRegistration {

    private String serviceId;
    private String callbackPath = "/internal/flowable/callback";

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getCallbackPath() {
        return callbackPath;
    }

    public void setCallbackPath(String callbackPath) {
        this.callbackPath = callbackPath;
    }
}
