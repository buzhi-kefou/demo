package com.parallel.learn.flowable.starter;

import com.parallel.learn.flowable.core.deployment.FlowableDeploymentProperties;
import com.parallel.learn.flowable.core.logging.FlowableLoggingProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "parallel.flowable")
public class ParallelFlowableProperties {

    private final FlowableDeploymentProperties deployment = new FlowableDeploymentProperties();

    private final FlowableLoggingProperties logging = new FlowableLoggingProperties();

    public FlowableDeploymentProperties getDeployment() {
        return deployment;
    }

    public FlowableLoggingProperties getLogging() {
        return logging;
    }
}
