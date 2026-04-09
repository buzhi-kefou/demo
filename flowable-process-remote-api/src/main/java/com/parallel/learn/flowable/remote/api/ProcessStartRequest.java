package com.parallel.learn.flowable.remote.api;

import java.util.Map;

public class ProcessStartRequest {

    private String processDefinitionKey;
    private String businessKey;
    private Map<String, Object> variables;
    private BusinessCallbackRegistration callbackRegistration;

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public BusinessCallbackRegistration getCallbackRegistration() {
        return callbackRegistration;
    }

    public void setCallbackRegistration(BusinessCallbackRegistration callbackRegistration) {
        this.callbackRegistration = callbackRegistration;
    }
}
