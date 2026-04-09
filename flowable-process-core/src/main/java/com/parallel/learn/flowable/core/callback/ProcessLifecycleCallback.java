package com.parallel.learn.flowable.core.callback;

public interface ProcessLifecycleCallback {

    String processDefinitionKey();

    default void onProcessStarted(ProcessCallbackContext context) {
    }

    default void onTaskCreated(ProcessCallbackContext context) {
    }

    default void onTaskCompleted(ProcessCallbackContext context) {
    }

    default void onProcessCompleted(ProcessCallbackContext context) {
    }

    default void onProcessCancelled(ProcessCallbackContext context) {
    }
}
