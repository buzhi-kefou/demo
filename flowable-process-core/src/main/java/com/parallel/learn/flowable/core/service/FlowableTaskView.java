package com.parallel.learn.flowable.core.service;

public class FlowableTaskView {

    private final String taskId;
    private final String taskName;
    private final String assignee;
    private final String processInstanceId;
    private final String processDefinitionId;

    public FlowableTaskView(String taskId,
                            String taskName,
                            String assignee,
                            String processInstanceId,
                            String processDefinitionId) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.assignee = assignee;
        this.processInstanceId = processInstanceId;
        this.processDefinitionId = processDefinitionId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getAssignee() {
        return assignee;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }
}
