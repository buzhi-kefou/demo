package com.parallel.learn.sentinel.web;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SentinelFallbacks {

    private SentinelFallbacks() {
    }

    public static Map<String, Object> degradeFallback(String mode, Throwable ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("type", "FALLBACK");
        body.put("resource", "degradeByException");
        body.put("mode", mode);
        body.put("message", "Business fallback executed.");
        body.put("exception", ex == null ? null : ex.getClass().getSimpleName());
        return body;
    }
}
