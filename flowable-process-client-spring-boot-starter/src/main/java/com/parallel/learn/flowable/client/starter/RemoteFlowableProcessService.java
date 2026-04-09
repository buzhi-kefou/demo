package com.parallel.learn.flowable.client.starter;

import java.util.List;
import java.util.Map;

import com.parallel.learn.flowable.remote.api.ProcessInstanceResponse;
import com.parallel.learn.flowable.remote.api.ProcessTaskResponse;

public interface RemoteFlowableProcessService {

    ProcessInstanceResponse startProcess(String processDefinitionKey, String businessKey, Map<String, Object> variables);

    List<ProcessTaskResponse> queryTasks(String processInstanceId, String assignee);

    void completeTask(String taskId, Map<String, Object> variables);
}
