package com.parallel.learn.flowable.center.controller;

import java.util.List;
import java.util.Map;

import com.parallel.learn.flowable.center.service.ProcessCenterRemoteService;
import com.parallel.learn.flowable.remote.api.ProcessInstanceResponse;
import com.parallel.learn.flowable.remote.api.ProcessStartRequest;
import com.parallel.learn.flowable.remote.api.ProcessTaskCompleteRequest;
import com.parallel.learn.flowable.remote.api.ProcessTaskResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/remote/process")
public class ProcessCenterController {

    private final ProcessCenterRemoteService processCenterRemoteService;

    public ProcessCenterController(ProcessCenterRemoteService processCenterRemoteService) {
        this.processCenterRemoteService = processCenterRemoteService;
    }

    @PostMapping
    public ProcessInstanceResponse start(@RequestBody ProcessStartRequest request) {
        return processCenterRemoteService.startProcess(request);
    }

    @GetMapping("/{processInstanceId}/tasks")
    public List<ProcessTaskResponse> tasks(@PathVariable String processInstanceId,
                                           @RequestParam(required = false) String assignee) {
        return processCenterRemoteService.queryTasks(processInstanceId, assignee);
    }

    @PostMapping("/task/{taskId}/complete")
    public Map<String, String> complete(@PathVariable String taskId,
                                        @RequestBody(required = false) ProcessTaskCompleteRequest request) {
        processCenterRemoteService.completeTask(taskId, request == null ? null : request.getVariables());
        return Map.of("taskId", taskId, "status", "COMPLETED");
    }
}
