package com.parallel.learn.flowable.center.config;

import java.util.List;

import com.parallel.learn.flowable.center.callback.BusinessCallbackNotifier;
import com.parallel.learn.flowable.center.listener.RemoteProcessEventListener;
import com.parallel.learn.flowable.center.service.ProcessCenterRemoteService;
import com.parallel.learn.flowable.core.deployment.FlowableXmlDeploymentRunner;
import com.parallel.learn.flowable.core.service.DefaultFlowableProcessService;
import com.parallel.learn.flowable.core.service.FlowableProcessService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ProcessCenterProperties.class)
public class ProcessCenterConfiguration {

    @Bean
    public FlowableProcessService flowableProcessService(RuntimeService runtimeService, TaskService taskService) {
        return new DefaultFlowableProcessService(runtimeService, taskService);
    }

    @Bean
    public FlowableXmlDeploymentRunner flowableXmlDeploymentRunner(RepositoryService repositoryService,
                                                                   ProcessCenterProperties properties) {
        return new FlowableXmlDeploymentRunner(repositoryService, properties.getDeployment());
    }

    @Bean
    @LoadBalanced
    public RestTemplate processCallbackRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public BusinessCallbackNotifier businessCallbackNotifier(RestTemplate processCallbackRestTemplate,
                                                             DiscoveryClient discoveryClient,
                                                             ObjectProvider<HistoryService> historyService) {
        return new BusinessCallbackNotifier(processCallbackRestTemplate, discoveryClient, historyService);
    }

    @Bean
    public RemoteProcessEventListener remoteProcessEventListener(ObjectProvider<RuntimeService> runtimeService,
                                                                 ObjectProvider<HistoryService> historyService,
                                                                 BusinessCallbackNotifier notifier,
                                                                 ProcessCenterProperties properties) {
        return new RemoteProcessEventListener(runtimeService, historyService, notifier, properties.getLogging());
    }

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> processEngineConfigurationConfigurer(
        RemoteProcessEventListener listener) {
        return configuration -> configuration.setEventListeners(List.of(listener));
    }

    @Bean
    public ProcessCenterRemoteService processCenterRemoteService(FlowableProcessService flowableProcessService) {
        return new ProcessCenterRemoteService(flowableProcessService);
    }
}
