package com.parallel.learn.flowable.core.logging;

import java.util.Collections;
import java.util.Map;

import com.parallel.learn.flowable.core.callback.ProcessCallbackContext;
import com.parallel.learn.flowable.core.callback.ProcessCallbackDispatcher;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.event.FlowableEntityWithVariablesEvent;
import org.flowable.engine.delegate.event.FlowableProcessStartedEvent;
import org.flowable.engine.delegate.event.FlowableProcessTerminatedEvent;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.beans.factory.ObjectProvider;

public class FlowableProcessEventListener implements FlowableEventListener {

    private static final Logger log = LoggerFactory.getLogger(FlowableProcessEventListener.class);

    private final ObjectProvider<RuntimeService> runtimeServiceProvider;
    private final ObjectProvider<HistoryService> historyServiceProvider;
    private final ProcessCallbackDispatcher dispatcher;
    private final FlowableLoggingProperties properties;

    public FlowableProcessEventListener(ObjectProvider<RuntimeService> runtimeServiceProvider,
                                        ObjectProvider<HistoryService> historyServiceProvider,
                                        ProcessCallbackDispatcher dispatcher,
                                        FlowableLoggingProperties properties) {
        this.runtimeServiceProvider = runtimeServiceProvider;
        this.historyServiceProvider = historyServiceProvider;
        this.dispatcher = dispatcher;
        this.properties = properties;
    }

    @Override
    public void onEvent(FlowableEvent event) {
        FlowableEngineEventType type = (FlowableEngineEventType) event.getType();
        FlowableEngineEvent engineEvent = event instanceof FlowableEngineEvent ? (FlowableEngineEvent) event : null;
        if (properties.isDetailedEventLogEnabled()) {
            log.info("Flowable event={}, processInstanceId={}, executionId={}",
                type.name(),
                engineEvent == null ? null : engineEvent.getProcessInstanceId(),
                engineEvent == null ? null : engineEvent.getExecutionId());
        }
        switch (type) {
            case PROCESS_STARTED:
                dispatcher.onProcessStarted(buildContext((FlowableProcessStartedEvent) event, null));
                break;
            case TASK_CREATED:
                dispatcher.onTaskCreated(buildTaskContext((FlowableEntityEvent) event));
                break;
            case TASK_COMPLETED:
                dispatcher.onTaskCompleted(buildTaskContext((FlowableEntityEvent) event));
                break;
            case PROCESS_COMPLETED:
                if (engineEvent != null) {
                    dispatcher.onProcessCompleted(buildContext(
                        engineEvent.getProcessInstanceId(),
                        engineEvent.getExecutionId(),
                        null));
                }
                break;
            case PROCESS_CANCELLED:
                dispatcher.onProcessCancelled(buildTerminatedContext((FlowableProcessTerminatedEvent) event));
                break;
            default:
                break;
        }
    }

    private ProcessCallbackContext buildTaskContext(FlowableEntityEvent event) {
        if (!(event.getEntity() instanceof TaskEntity taskEntity)) {
            return new ProcessCallbackContext("TASK_EVENT", null, null, null, null, null, Collections.emptyMap());
        }
        Map<String, Object> variables = taskEntity.getExecutionId() == null
            ? Collections.emptyMap()
            : getRuntimeService().getVariables(taskEntity.getExecutionId());
        return buildContext(taskEntity.getProcessInstanceId(), taskEntity.getExecutionId(), taskEntity.getId(), variables);
    }

    private ProcessCallbackContext buildContext(FlowableProcessStartedEvent event, String taskId) {
        FlowableEntityWithVariablesEvent entityEvent = event;
        FlowableEngineEvent engineEvent = (FlowableEngineEvent) event;
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = entityEvent.getVariables();
        return buildContext(engineEvent.getProcessInstanceId(), engineEvent.getExecutionId(), taskId, variables);
    }

    private ProcessCallbackContext buildTerminatedContext(FlowableProcessTerminatedEvent event) {
        if (event.getEntity() instanceof ProcessInstance processInstance) {
            return buildContext(processInstance.getProcessInstanceId(), processInstance.getProcessInstanceId(), null);
        }
        return new ProcessCallbackContext("PROCESS_EVENT", null, null, null, null, null, Collections.emptyMap());
    }

    private ProcessCallbackContext buildContext(String processInstanceId, String executionId, String taskId) {
        Map<String, Object> variables = executionId == null ? Collections.emptyMap() : getRuntimeService().getVariables(executionId);
        return buildContext(processInstanceId, executionId, taskId, variables);
    }

    private ProcessCallbackContext buildContext(String processInstanceId,
                                                String executionId,
                                                String taskId,
                                                Map<String, Object> variables) {
        HistoricProcessInstance historic = getHistoryService().createHistoricProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();
        String definitionKey = historic == null ? null : historic.getProcessDefinitionKey();
        String businessKey = historic == null ? null : historic.getBusinessKey();
        String eventType = taskId == null ? "PROCESS_EVENT" : "TASK_EVENT";
        return new ProcessCallbackContext(eventType, definitionKey, processInstanceId, executionId, taskId, businessKey, variables);
    }

    private RuntimeService getRuntimeService() {
        return runtimeServiceProvider.getObject();
    }

    private HistoryService getHistoryService() {
        return historyServiceProvider.getObject();
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }
}
