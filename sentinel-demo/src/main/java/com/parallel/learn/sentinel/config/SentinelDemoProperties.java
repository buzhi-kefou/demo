package com.parallel.learn.sentinel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "demo.sentinel")
public class SentinelDemoProperties {

    private long degradeDelayMs = 800L;

    public long getDegradeDelayMs() {
        return degradeDelayMs;
    }

    public void setDegradeDelayMs(long degradeDelayMs) {
        this.degradeDelayMs = degradeDelayMs;
    }
}
