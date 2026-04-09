package com.parallel.learn.flowable.client.starter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.parallel.learn.flowable.remote.api.BusinessCallbackRegistration;
import com.parallel.learn.flowable.remote.api.ProcessInstanceResponse;
import com.parallel.learn.flowable.remote.api.ProcessStartRequest;
import com.parallel.learn.flowable.remote.api.ProcessTaskCompleteRequest;
import com.parallel.learn.flowable.remote.api.ProcessTaskResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class DefaultRemoteFlowableProcessService implements RemoteFlowableProcessService {

    private final RestTemplate restTemplate;
    private final FlowableProcessClientProperties properties;
    private final String applicationName;

    public DefaultRemoteFlowableProcessService(RestTemplate restTemplate,
                                               FlowableProcessClientProperties properties,
                                               String applicationName) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.applicationName = applicationName;
    }

    @Override
    public ProcessInstanceResponse startProcess(String processDefinitionKey,
                                                String businessKey,
                                                Map<String, Object> variables) {
        ProcessStartRequest request = new ProcessStartRequest();
        request.setProcessDefinitionKey(processDefinitionKey);
        request.setBusinessKey(businessKey);
        request.setVariables(variables == null ? Collections.emptyMap() : variables);
        if (properties.isRegisterCallback()) {
            BusinessCallbackRegistration registration = new BusinessCallbackRegistration();
            registration.setServiceId(applicationName);
            registration.setCallbackPath(properties.getCallbackPath());
            request.setCallbackRegistration(registration);
        }
        return restTemplate.postForObject(baseUrl(), request, ProcessInstanceResponse.class);
    }

    @Override
    public List<ProcessTaskResponse> queryTasks(String processInstanceId, String assignee) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl() + "/" + processInstanceId + "/tasks")
            .queryParam("assignee", assignee)
            .build()
            .toUriString();
        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<List<ProcessTaskResponse>>() {
                })
            .getBody();
    }

    @Override
    public void completeTask(String taskId, Map<String, Object> variables) {
        ProcessTaskCompleteRequest request = new ProcessTaskCompleteRequest();
        request.setVariables(variables == null ? Collections.emptyMap() : variables);
        restTemplate.postForLocation(baseUrl() + "/task/" + taskId + "/complete", request);
    }

    private String baseUrl() {
        return "http://" + properties.getProcessServiceId() + properties.getBasePath();
    }
}
