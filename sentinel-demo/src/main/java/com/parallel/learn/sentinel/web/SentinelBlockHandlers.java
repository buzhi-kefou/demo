package com.parallel.learn.sentinel.web;

import java.util.LinkedHashMap;
import java.util.Map;

import com.alibaba.csp.sentinel.slots.block.BlockException;

public final class SentinelBlockHandlers {

    private SentinelBlockHandlers() {
    }

    public static Map<String, Object> handleHello(String name, BlockException ex) {
        return build("helloResource", ex, Map.of("name", name));
    }

    public static Map<String, Object> handleHotParam(Long userId, String skuId, BlockException ex) {
        return build("hotParamResource", ex, Map.of("userId", userId, "skuId", skuId));
    }

    public static Map<String, Object> handleDegrade(String mode, BlockException ex) {
        return build("degradeByException", ex, Map.of("mode", mode));
    }

    private static Map<String, Object> build(String resource, BlockException ex, Map<String, Object> extra) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("type", "BLOCKED");
        body.put("resource", resource);
        body.put("exception", ex.getClass().getSimpleName());
        body.put("message", "Request was blocked by Sentinel rule.");
        body.putAll(extra);
        return body;
    }
}
