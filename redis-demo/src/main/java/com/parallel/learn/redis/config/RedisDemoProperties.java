package com.parallel.learn.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "demo.redis")
public class RedisDemoProperties {

    /**
     * Common key prefix used by this module.
     */
    private String keyPrefix = "demo:redis:";

    /**
     * Default TTL in seconds for sample cache data.
     */
    private long defaultTtlSeconds = 300;

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public long getDefaultTtlSeconds() {
        return defaultTtlSeconds;
    }

    public void setDefaultTtlSeconds(long defaultTtlSeconds) {
        this.defaultTtlSeconds = defaultTtlSeconds;
    }
}
