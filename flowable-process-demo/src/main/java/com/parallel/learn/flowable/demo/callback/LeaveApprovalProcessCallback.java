package com.parallel.learn.flowable.demo.callback;

import com.parallel.learn.flowable.core.callback.ProcessCallbackContext;
import com.parallel.learn.flowable.core.callback.ProcessLifecycleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LeaveApprovalProcessCallback implements ProcessLifecycleCallback {

    private static final Logger log = LoggerFactory.getLogger(LeaveApprovalProcessCallback.class);

    @Override
    public String processDefinitionKey() {
        return "leaveApproval";
    }

    @Override
    public void onProcessStarted(ProcessCallbackContext context) {
        log.info("Business callback - process started, businessKey={}, variables={}",
            context.getBusinessKey(), context.getVariables());
    }

    @Override
    public void onTaskCreated(ProcessCallbackContext context) {
        log.info("Business callback - task created, taskId={}, processInstanceId={}",
            context.getTaskId(), context.getProcessInstanceId());
    }

    @Override
    public void onTaskCompleted(ProcessCallbackContext context) {
        log.info("Business callback - task completed, taskId={}, processInstanceId={}",
            context.getTaskId(), context.getProcessInstanceId());
    }

    @Override
    public void onProcessCompleted(ProcessCallbackContext context) {
        log.info("Business callback - process completed, businessKey={}, processInstanceId={}",
            context.getBusinessKey(), context.getProcessInstanceId());
    }
}
