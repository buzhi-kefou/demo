package com.parallel.learn.flowable.core.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultFlowableProcessService implements FlowableProcessService {

    private static final Logger log = LoggerFactory.getLogger(DefaultFlowableProcessService.class);

    private final RuntimeService runtimeService;
    private final TaskService taskService;

    public DefaultFlowableProcessService(RuntimeService runtimeService, TaskService taskService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    @Override
    public FlowableProcessInstanceView startProcess(String processDefinitionKey,
                                                    String businessKey,
                                                    Map<String, Object> variables) {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
            processDefinitionKey, businessKey, variables == null ? Collections.emptyMap() : variables);
        log.info("Started Flowable process, processDefinitionKey={}, processInstanceId={}, businessKey={}",
            processDefinitionKey, processInstance.getProcessInstanceId(), businessKey);
        return new FlowableProcessInstanceView(
            processInstance.getProcessInstanceId(),
            processInstance.getProcessDefinitionId(),
            processInstance.getProcessDefinitionKey(),
            processInstance.getBusinessKey(),
            runtimeService.getVariables(processInstance.getProcessInstanceId()));
    }

    @Override
    public List<FlowableTaskView> queryTasks(String processInstanceId, String assignee) {
        var query = taskService.createTaskQuery().processInstanceId(processInstanceId);
        if (assignee != null && !assignee.isBlank()) {
            query.taskAssignee(assignee);
        }
        return query.list()
            .stream()
            .map(task -> new FlowableTaskView(
                task.getId(),
                task.getName(),
                task.getAssignee(),
                task.getProcessInstanceId(),
                task.getProcessDefinitionId()))
            .toList();
    }

    @Override
    public void completeTask(String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables == null ? Collections.emptyMap() : variables);
        log.info("Completed Flowable task, taskId={}", taskId);
    }
}
