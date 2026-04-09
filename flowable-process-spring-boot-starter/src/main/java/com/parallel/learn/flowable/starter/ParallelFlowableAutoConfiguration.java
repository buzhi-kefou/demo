package com.parallel.learn.flowable.starter;

import java.util.List;

import com.parallel.learn.flowable.core.callback.ProcessCallbackDispatcher;
import com.parallel.learn.flowable.core.callback.ProcessLifecycleCallback;
import com.parallel.learn.flowable.core.deployment.FlowableXmlDeploymentRunner;
import com.parallel.learn.flowable.core.logging.FlowableProcessEventListener;
import com.parallel.learn.flowable.core.service.DefaultFlowableProcessService;
import com.parallel.learn.flowable.core.service.FlowableProcessService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ParallelFlowableProperties.class)
@AutoConfigureAfter(name = "org.flowable.spring.boot.ProcessEngineAutoConfiguration")
public class ParallelFlowableAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ProcessCallbackDispatcher processCallbackDispatcher(ObjectProvider<List<ProcessLifecycleCallback>> callbacks) {
        return new ProcessCallbackDispatcher(callbacks.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowableProcessService flowableProcessService(RuntimeService runtimeService, TaskService taskService) {
        return new DefaultFlowableProcessService(runtimeService, taskService);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowableProcessEventListener flowableProcessEventListener(ObjectProvider<RuntimeService> runtimeService,
                                                                     ObjectProvider<HistoryService> historyService,
                                                                     ProcessCallbackDispatcher dispatcher,
                                                                     ParallelFlowableProperties properties) {
        return new FlowableProcessEventListener(
            runtimeService,
            historyService,
            dispatcher,
            properties.getLogging());
    }

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> parallelFlowableEngineConfigurer(
        FlowableProcessEventListener listener) {
        return configuration -> configuration.setEventListeners(List.of(listener));
    }

    @Bean
    @ConditionalOnProperty(prefix = "parallel.flowable.deployment", name = "auto-deploy-enabled", havingValue = "true", matchIfMissing = true)
    public FlowableXmlDeploymentRunner flowableXmlDeploymentRunner(RepositoryService repositoryService,
                                                                   ParallelFlowableProperties properties) {
        return new FlowableXmlDeploymentRunner(repositoryService, properties.getDeployment());
    }
}
