package com.parallel.learn.flowable.core.service;

import java.util.List;
import java.util.Map;

public interface FlowableProcessService {

    FlowableProcessInstanceView startProcess(String processDefinitionKey, String businessKey, Map<String, Object> variables);

    List<FlowableTaskView> queryTasks(String processInstanceId, String assignee);

    void completeTask(String taskId, Map<String, Object> variables);
}
