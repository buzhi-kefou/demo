package com.parallel.learn.flowable.core.deployment;

import java.util.ArrayList;
import java.util.List;

public class FlowableDeploymentProperties {

    private boolean autoDeployEnabled = true;

    private String deploymentName = "shared-flowable-deployment";

    private List<String> resourceLocations = new ArrayList<>(List.of("classpath*:/processes/**/*.bpmn20.xml"));

    public boolean isAutoDeployEnabled() {
        return autoDeployEnabled;
    }

    public void setAutoDeployEnabled(boolean autoDeployEnabled) {
        this.autoDeployEnabled = autoDeployEnabled;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public List<String> getResourceLocations() {
        return resourceLocations;
    }

    public void setResourceLocations(List<String> resourceLocations) {
        this.resourceLocations = resourceLocations;
    }
}
