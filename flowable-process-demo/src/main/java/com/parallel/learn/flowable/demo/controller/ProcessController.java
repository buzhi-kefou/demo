package com.parallel.learn.flowable.demo.controller;

import java.util.List;
import java.util.Map;

import com.parallel.learn.flowable.client.starter.RemoteFlowableProcessService;
import com.parallel.learn.flowable.demo.model.CompleteTaskRequest;
import com.parallel.learn.flowable.demo.model.StartProcessRequest;
import com.parallel.learn.flowable.remote.api.ProcessInstanceResponse;
import com.parallel.learn.flowable.remote.api.ProcessTaskResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/process")
public class ProcessController {

    private final RemoteFlowableProcessService remoteFlowableProcessService;

    public ProcessController(RemoteFlowableProcessService remoteFlowableProcessService) {
        this.remoteFlowableProcessService = remoteFlowableProcessService;
    }

    @PostMapping("/start")
    public ProcessInstanceResponse start(@RequestBody StartProcessRequest request) {
        return remoteFlowableProcessService.startProcess(
            request.getProcessDefinitionKey(),
            request.getBusinessKey(),
            request.getVariables());
    }

    @GetMapping("/{processInstanceId}/tasks")
    public List<ProcessTaskResponse> tasks(@PathVariable String processInstanceId,
                                           @RequestParam(required = false) String assignee) {
        return remoteFlowableProcessService.queryTasks(processInstanceId, assignee);
    }

    @PostMapping("/task/{taskId}/complete")
    public Map<String, String> complete(@PathVariable String taskId,
                                        @RequestBody(required = false) CompleteTaskRequest request) {
        remoteFlowableProcessService.completeTask(taskId, request == null ? null : request.getVariables());
        return Map.of("taskId", taskId, "status", "COMPLETED");
    }
}
