package com.parallel.learn.flowable.center.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.parallel.learn.flowable.core.service.FlowableProcessInstanceView;
import com.parallel.learn.flowable.core.service.FlowableProcessService;
import com.parallel.learn.flowable.core.service.FlowableTaskView;
import com.parallel.learn.flowable.remote.api.BusinessCallbackRegistration;
import com.parallel.learn.flowable.remote.api.ProcessInstanceResponse;
import com.parallel.learn.flowable.remote.api.ProcessStartRequest;
import com.parallel.learn.flowable.remote.api.ProcessTaskResponse;

public class ProcessCenterRemoteService {

    public static final String CALLBACK_SERVICE_ID = "_callbackServiceId";
    public static final String CALLBACK_PATH = "_callbackPath";

    private final FlowableProcessService flowableProcessService;

    public ProcessCenterRemoteService(FlowableProcessService flowableProcessService) {
        this.flowableProcessService = flowableProcessService;
    }

    public ProcessInstanceResponse startProcess(ProcessStartRequest request) {
        Map<String, Object> mergedVariables = new HashMap<>();
        if (request.getVariables() != null) {
            mergedVariables.putAll(request.getVariables());
        }
        BusinessCallbackRegistration registration = request.getCallbackRegistration();
        if (registration != null) {
            mergedVariables.put(CALLBACK_SERVICE_ID, registration.getServiceId());
            mergedVariables.put(CALLBACK_PATH, registration.getCallbackPath());
        }
        FlowableProcessInstanceView view = flowableProcessService.startProcess(
            request.getProcessDefinitionKey(),
            request.getBusinessKey(),
            mergedVariables);
        return toResponse(view);
    }

    public List<ProcessTaskResponse> queryTasks(String processInstanceId, String assignee) {
        return flowableProcessService.queryTasks(processInstanceId, assignee)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public void completeTask(String taskId, Map<String, Object> variables) {
        flowableProcessService.completeTask(taskId, variables);
    }

    private ProcessInstanceResponse toResponse(FlowableProcessInstanceView view) {
        ProcessInstanceResponse response = new ProcessInstanceResponse();
        response.setProcessInstanceId(view.getProcessInstanceId());
        response.setProcessDefinitionId(view.getProcessDefinitionId());
        response.setProcessDefinitionKey(view.getProcessDefinitionKey());
        response.setBusinessKey(view.getBusinessKey());
        response.setVariables(view.getVariables());
        return response;
    }

    private ProcessTaskResponse toResponse(FlowableTaskView view) {
        ProcessTaskResponse response = new ProcessTaskResponse();
        response.setTaskId(view.getTaskId());
        response.setTaskName(view.getTaskName());
        response.setAssignee(view.getAssignee());
        response.setProcessInstanceId(view.getProcessInstanceId());
        response.setProcessDefinitionId(view.getProcessDefinitionId());
        return response;
    }
}
