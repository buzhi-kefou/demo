package com.parallel.learn.flowable.center.callback;

import java.util.Map;

import com.parallel.learn.flowable.center.service.ProcessCenterRemoteService;
import com.parallel.learn.flowable.core.callback.ProcessCallbackContext;
import com.parallel.learn.flowable.remote.api.ProcessLifecycleEventRequest;
import org.flowable.engine.HistoryService;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

public class BusinessCallbackNotifier {

    private static final Logger log = LoggerFactory.getLogger(BusinessCallbackNotifier.class);

    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;
    private final ObjectProvider<HistoryService> historyServiceProvider;

    public BusinessCallbackNotifier(RestTemplate restTemplate,
                                    DiscoveryClient discoveryClient,
                                    ObjectProvider<HistoryService> historyServiceProvider) {
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
        this.historyServiceProvider = historyServiceProvider;
    }

    public void notify(ProcessCallbackContext context) {
        CallbackTarget target = resolveTarget(context);
        if (target == null) {
            log.debug("Skip business callback because no callback target was registered, processInstanceId={}",
                context.getProcessInstanceId());
            return;
        }
        if (discoveryClient.getInstances(target.serviceId()).isEmpty()) {
            log.warn("Skip business callback because service is not registered in discovery, serviceId={}",
                target.serviceId());
            return;
        }
        ProcessLifecycleEventRequest request = new ProcessLifecycleEventRequest();
        request.setEventType(context.getEventType());
        request.setProcessDefinitionKey(context.getProcessDefinitionKey());
        request.setProcessInstanceId(context.getProcessInstanceId());
        request.setExecutionId(context.getExecutionId());
        request.setTaskId(context.getTaskId());
        request.setBusinessKey(context.getBusinessKey());
        request.setVariables(context.getVariables());
        try {
            restTemplate.postForLocation("http://" + target.serviceId() + normalizePath(target.path()), request);
        } catch (RestClientException ex) {
            log.warn("Business callback invocation failed, serviceId={}, path={}, processInstanceId={}",
                target.serviceId(), normalizePath(target.path()), context.getProcessInstanceId(), ex);
        }
    }

    private CallbackTarget resolveTarget(ProcessCallbackContext context) {
        String serviceId = stringValue(context.getVariables().get(ProcessCenterRemoteService.CALLBACK_SERVICE_ID));
        String path = stringValue(context.getVariables().get(ProcessCenterRemoteService.CALLBACK_PATH));
        if (serviceId != null) {
            return new CallbackTarget(serviceId, path);
        }
        serviceId = readHistoricVariable(context.getProcessInstanceId(), ProcessCenterRemoteService.CALLBACK_SERVICE_ID);
        path = readHistoricVariable(context.getProcessInstanceId(), ProcessCenterRemoteService.CALLBACK_PATH);
        if (serviceId == null) {
            return null;
        }
        return new CallbackTarget(serviceId, path);
    }

    private String readHistoricVariable(String processInstanceId, String variableName) {
        if (processInstanceId == null) {
            return null;
        }
        HistoryService historyService = historyServiceProvider.getIfAvailable();
        if (historyService == null) {
            return null;
        }
        HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery()
            .processInstanceId(processInstanceId)
            .variableName(variableName)
            .singleResult();
        return historicVariable == null ? null : stringValue(historicVariable.getValue());
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return text.isBlank() ? null : text;
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/internal/flowable/callback";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private record CallbackTarget(String serviceId, String path) {
    }
}
