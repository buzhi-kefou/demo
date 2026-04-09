package com.parallel.learn.flowable.core.logging;

public class FlowableLoggingProperties {

    private boolean detailedEventLogEnabled = true;

    public boolean isDetailedEventLogEnabled() {
        return detailedEventLogEnabled;
    }

    public void setDetailedEventLogEnabled(boolean detailedEventLogEnabled) {
        this.detailedEventLogEnabled = detailedEventLogEnabled;
    }
}
