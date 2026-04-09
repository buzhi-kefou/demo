package com.parallel.learn.flowable.core.service;

import java.util.Map;

public class FlowableProcessInstanceView {

    private final String processInstanceId;
    private final String processDefinitionId;
    private final String processDefinitionKey;
    private final String businessKey;
    private final Map<String, Object> variables;

    public FlowableProcessInstanceView(String processInstanceId,
                                       String processDefinitionId,
                                       String processDefinitionKey,
                                       String businessKey,
                                       Map<String, Object> variables) {
        this.processInstanceId = processInstanceId;
        this.processDefinitionId = processDefinitionId;
        this.processDefinitionKey = processDefinitionKey;
        this.businessKey = businessKey;
        this.variables = variables;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }
}
