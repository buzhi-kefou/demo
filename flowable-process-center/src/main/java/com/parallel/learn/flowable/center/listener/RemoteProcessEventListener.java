package com.parallel.learn.flowable.center.listener;

import java.util.Collections;
import java.util.Map;

import com.parallel.learn.flowable.center.callback.BusinessCallbackNotifier;
import com.parallel.learn.flowable.core.callback.ProcessCallbackContext;
import com.parallel.learn.flowable.core.logging.FlowableLoggingProperties;
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
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;

public class RemoteProcessEventListener implements FlowableEventListener {

    private static final Logger log = LoggerFactory.getLogger(RemoteProcessEventListener.class);

    private final ObjectProvider<RuntimeService> runtimeServiceProvider;
    private final ObjectProvider<HistoryService> historyServiceProvider;
    private final BusinessCallbackNotifier notifier;
    private final FlowableLoggingProperties properties;

    public RemoteProcessEventListener(ObjectProvider<RuntimeService> runtimeServiceProvider,
                                      ObjectProvider<HistoryService> historyServiceProvider,
                                      BusinessCallbackNotifier notifier,
                                      FlowableLoggingProperties properties) {
        this.runtimeServiceProvider = runtimeServiceProvider;
        this.historyServiceProvider = historyServiceProvider;
        this.notifier = notifier;
        this.properties = properties;
    }

    @Override
    public void onEvent(FlowableEvent event) {
        FlowableEngineEventType type = (FlowableEngineEventType) event.getType();
        FlowableEngineEvent engineEvent = event instanceof FlowableEngineEvent ? (FlowableEngineEvent) event : null;
        if (properties.isDetailedEventLogEnabled()) {
            log.info("Flowable center event={}, processInstanceId={}, executionId={}",
                type.name(),
                engineEvent == null ? null : engineEvent.getProcessInstanceId(),
                engineEvent == null ? null : engineEvent.getExecutionId());
        }
        switch (type) {
            case PROCESS_STARTED:
                notifier.notify(buildContext((FlowableProcessStartedEvent) event, "PROCESS_STARTED", null));
                break;
            case TASK_CREATED:
                notifier.notify(buildTaskContext((FlowableEntityEvent) event, "TASK_CREATED"));
                break;
            case TASK_COMPLETED:
                notifier.notify(buildTaskContext((FlowableEntityEvent) event, "TASK_COMPLETED"));
                break;
            case PROCESS_COMPLETED:
                if (engineEvent != null) {
                    notifier.notify(buildContext(
                        engineEvent.getProcessInstanceId(),
                        engineEvent.getExecutionId(),
                        null,
                        "PROCESS_COMPLETED"));
                }
                break;
            case PROCESS_CANCELLED:
                notifier.notify(buildTerminatedContext((FlowableProcessTerminatedEvent) event));
                break;
            default:
                break;
        }
    }

    private ProcessCallbackContext buildTaskContext(FlowableEntityEvent event, String eventType) {
        if (!(event.getEntity() instanceof TaskEntity taskEntity)) {
            return new ProcessCallbackContext(eventType, null, null, null, null, null, Collections.emptyMap());
        }
        Map<String, Object> variables = safeRuntimeVariables(taskEntity.getExecutionId());
        return buildContext(taskEntity.getProcessInstanceId(), taskEntity.getExecutionId(), taskEntity.getId(), eventType, variables);
    }

    private ProcessCallbackContext buildContext(FlowableProcessStartedEvent event, String eventType, String taskId) {
        FlowableEntityWithVariablesEvent entityEvent = event;
        FlowableEngineEvent engineEvent = (FlowableEngineEvent) event;
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = entityEvent.getVariables();
        return buildContext(engineEvent.getProcessInstanceId(), engineEvent.getExecutionId(), taskId, eventType, variables);
    }

    private ProcessCallbackContext buildTerminatedContext(FlowableProcessTerminatedEvent event) {
        if (event.getEntity() instanceof ProcessInstance processInstance) {
            return buildContext(
                processInstance.getProcessInstanceId(),
                processInstance.getProcessInstanceId(),
                null,
                "PROCESS_CANCELLED");
        }
        return new ProcessCallbackContext("PROCESS_CANCELLED", null, null, null, null, null, Collections.emptyMap());
    }

    private ProcessCallbackContext buildContext(String processInstanceId,
                                                String executionId,
                                                String taskId,
                                                String eventType) {
        Map<String, Object> variables = safeRuntimeVariables(executionId);
        return buildContext(processInstanceId, executionId, taskId, eventType, variables);
    }

    private Map<String, Object> safeRuntimeVariables(String executionId) {
        if (executionId == null) {
            return Collections.emptyMap();
        }
        try {
            return getRuntimeService().getVariables(executionId);
        } catch (RuntimeException ex) {
            log.debug("Unable to load runtime variables for executionId={}", executionId, ex);
            return Collections.emptyMap();
        }
    }

    private ProcessCallbackContext buildContext(String processInstanceId,
                                                String executionId,
                                                String taskId,
                                                String eventType,
                                                Map<String, Object> variables) {
        HistoricProcessInstance historic = getHistoryService().createHistoricProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();
        String definitionKey = historic == null ? null : historic.getProcessDefinitionKey();
        String businessKey = historic == null ? null : historic.getBusinessKey();
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
