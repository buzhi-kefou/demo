package com.parallel.learn.flowable.core.callback;

import java.util.Collections;
import java.util.Map;

public class ProcessCallbackContext {

    private final String eventType;
    private final String processDefinitionKey;
    private final String processInstanceId;
    private final String executionId;
    private final String taskId;
    private final String businessKey;
    private final Map<String, Object> variables;

    public ProcessCallbackContext(String eventType,
                                  String processDefinitionKey,
                                  String processInstanceId,
                                  String executionId,
                                  String taskId,
                                  String businessKey,
                                  Map<String, Object> variables) {
        this.eventType = eventType;
        this.processDefinitionKey = processDefinitionKey;
        this.processInstanceId = processInstanceId;
        this.executionId = executionId;
        this.taskId = taskId;
        this.businessKey = businessKey;
        this.variables = variables == null ? Collections.emptyMap() : Collections.unmodifiableMap(variables);
    }

    public String getEventType() {
        return eventType;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }
}
