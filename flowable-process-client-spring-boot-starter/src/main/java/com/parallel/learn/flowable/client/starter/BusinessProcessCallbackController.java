package com.parallel.learn.flowable.client.starter;

import com.parallel.learn.flowable.core.callback.ProcessCallbackContext;
import com.parallel.learn.flowable.core.callback.ProcessCallbackDispatcher;
import com.parallel.learn.flowable.remote.api.ProcessLifecycleEventRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${parallel.flowable.client.callback-path:/internal/flowable/callback}")
public class BusinessProcessCallbackController {

    private final ProcessCallbackDispatcher dispatcher;

    public BusinessProcessCallbackController(ProcessCallbackDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostMapping
    public ResponseEntity<Void> onCallback(@RequestBody ProcessLifecycleEventRequest request) {
        ProcessCallbackContext context = new ProcessCallbackContext(
            request.getEventType(),
            request.getProcessDefinitionKey(),
            request.getProcessInstanceId(),
            request.getExecutionId(),
            request.getTaskId(),
            request.getBusinessKey(),
            request.getVariables());
        switch (request.getEventType()) {
            case "PROCESS_STARTED":
                dispatcher.onProcessStarted(context);
                break;
            case "TASK_CREATED":
                dispatcher.onTaskCreated(context);
                break;
            case "TASK_COMPLETED":
                dispatcher.onTaskCompleted(context);
                break;
            case "PROCESS_COMPLETED":
                dispatcher.onProcessCompleted(context);
                break;
            case "PROCESS_CANCELLED":
                dispatcher.onProcessCancelled(context);
                break;
            default:
                break;
        }
        return ResponseEntity.accepted().build();
    }
}
