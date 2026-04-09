package com.parallel.learn.flowable.core.callback;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessCallbackDispatcher {

    private static final Logger log = LoggerFactory.getLogger(ProcessCallbackDispatcher.class);

    private final Map<String, ProcessLifecycleCallback> callbackMap = new LinkedHashMap<>();

    public ProcessCallbackDispatcher(List<ProcessLifecycleCallback> callbacks) {
        if (callbacks != null) {
            for (ProcessLifecycleCallback callback : callbacks) {
                callbackMap.put(callback.processDefinitionKey(), callback);
            }
        }
    }

    public void onProcessStarted(ProcessCallbackContext context) {
        dispatch(context, ProcessLifecycleCallback::onProcessStarted);
    }

    public void onTaskCreated(ProcessCallbackContext context) {
        dispatch(context, ProcessLifecycleCallback::onTaskCreated);
    }

    public void onTaskCompleted(ProcessCallbackContext context) {
        dispatch(context, ProcessLifecycleCallback::onTaskCompleted);
    }

    public void onProcessCompleted(ProcessCallbackContext context) {
        dispatch(context, ProcessLifecycleCallback::onProcessCompleted);
    }

    public void onProcessCancelled(ProcessCallbackContext context) {
        dispatch(context, ProcessLifecycleCallback::onProcessCancelled);
    }

    private void dispatch(ProcessCallbackContext context, CallbackConsumer consumer) {
        ProcessLifecycleCallback callback = callbackMap.get(context.getProcessDefinitionKey());
        if (callback == null) {
            log.debug("No callback registered for processDefinitionKey={}", context.getProcessDefinitionKey());
            return;
        }
        consumer.accept(callback, context);
    }

    @FunctionalInterface
    private interface CallbackConsumer {
        void accept(ProcessLifecycleCallback callback, ProcessCallbackContext context);
    }
}
