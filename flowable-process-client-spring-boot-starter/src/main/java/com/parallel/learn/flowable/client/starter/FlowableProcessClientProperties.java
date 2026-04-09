package com.parallel.learn.flowable.client.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "parallel.flowable.client")
public class FlowableProcessClientProperties {

    private String processServiceId = "flowable-process-center";
    private String basePath = "/api/remote/process";
    private String callbackPath = "/internal/flowable/callback";
    private boolean registerCallback = true;

    public String getProcessServiceId() {
        return processServiceId;
    }

    public void setProcessServiceId(String processServiceId) {
        this.processServiceId = processServiceId;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getCallbackPath() {
        return callbackPath;
    }

    public void setCallbackPath(String callbackPath) {
        this.callbackPath = callbackPath;
    }

    public boolean isRegisterCallback() {
        return registerCallback;
    }

    public void setRegisterCallback(boolean registerCallback) {
        this.registerCallback = registerCallback;
    }
}
