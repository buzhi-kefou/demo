package com.parallel.learn.flowable.remote.api;

import java.util.Map;

public class ProcessTaskCompleteRequest {

    private Map<String, Object> variables;

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}
