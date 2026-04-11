package com.parallel.learn.sentinel.service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.parallel.learn.sentinel.config.SentinelDemoProperties;
import com.parallel.learn.sentinel.web.SentinelBlockHandlers;
import com.parallel.learn.sentinel.web.SentinelFallbacks;
import org.springframework.stereotype.Service;

@Service
public class SentinelDemoService {

    private final SentinelDemoProperties properties;

    public SentinelDemoService(SentinelDemoProperties properties) {
        this.properties = properties;
    }

    @SentinelResource(value = "helloResource", blockHandlerClass = SentinelBlockHandlers.class, blockHandler = "handleHello")
    public Map<String, Object> hello(String name) {
        return success("helloResource", Map.of(
            "message", "Hello, " + name,
            "timestamp", Instant.now().toString()));
    }

    @SentinelResource(value = "hotParamResource", blockHandlerClass = SentinelBlockHandlers.class, blockHandler = "handleHotParam")
    public Map<String, Object> hotParam(Long userId, String skuId) {
        return success("hotParamResource", Map.of(
            "userId", userId,
            "skuId", skuId,
            "tip", "Use Sentinel dashboard to configure hotspot rules on userId."));
    }

    @SentinelResource(
        value = "degradeByException",
        blockHandlerClass = SentinelBlockHandlers.class,
        blockHandler = "handleDegrade",
        fallbackClass = SentinelFallbacks.class,
        fallback = "degradeFallback")
    public Map<String, Object> degrade(String mode) {
        if ("slow".equalsIgnoreCase(mode)) {
            sleep(properties.getDegradeDelayMs());
            return success("degradeByException", Map.of(
                "mode", "slow",
                "delayMs", properties.getDegradeDelayMs(),
                "tip", "Configure RT-based degrade rule for resource degradeByException."));
        }
        if ("error".equalsIgnoreCase(mode)) {
            throw new IllegalStateException("Simulated business exception for Sentinel fallback.");
        }
        return success("degradeByException", Map.of(
            "mode", "ok",
            "tip", "Try mode=slow or mode=error to observe degrade and fallback behavior."));
    }

    public Map<String, Object> manualEntry(String name) {
        Entry entry = null;
        try {
            entry = SphU.entry("manualEntryResource");
            return success("manualEntryResource", Map.of(
                "message", "Manual Sentinel API passed for " + name,
                "tip", "This endpoint demonstrates SphU.entry manual resource definition."));
        } catch (BlockException ex) {
            return SentinelBlockHandlers.handleHello(name, ex);
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    private void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Sleep interrupted", ex);
        }
    }

    private Map<String, Object> success(String resource, Map<String, Object> extra) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("resource", resource);
        body.putAll(extra);
        return body;
    }
}
