package com.parallel.learn.flowable.client.starter;

import java.util.List;

import com.parallel.learn.flowable.core.callback.ProcessCallbackDispatcher;
import com.parallel.learn.flowable.core.callback.ProcessLifecycleCallback;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RestTemplate.class)
@EnableConfigurationProperties(FlowableProcessClientProperties.class)
public class FlowableProcessClientAutoConfiguration {

    @Bean
    @LoadBalanced
    @ConditionalOnMissingBean(name = "flowableProcessClientRestTemplate")
    public RestTemplate flowableProcessClientRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessCallbackDispatcher processCallbackDispatcher(ObjectProvider<List<ProcessLifecycleCallback>> callbacks) {
        return new ProcessCallbackDispatcher(callbacks.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public RemoteFlowableProcessService remoteFlowableProcessService(
        RestTemplate flowableProcessClientRestTemplate,
        FlowableProcessClientProperties properties,
        @Value("${spring.application.name}") String applicationName) {
        return new DefaultRemoteFlowableProcessService(flowableProcessClientRestTemplate, properties, applicationName);
    }

    @Bean
    @ConditionalOnProperty(prefix = "parallel.flowable.client", name = "register-callback", havingValue = "true", matchIfMissing = true)
    public BusinessProcessCallbackController businessProcessCallbackController(ProcessCallbackDispatcher dispatcher) {
        return new BusinessProcessCallbackController(dispatcher);
    }
}
