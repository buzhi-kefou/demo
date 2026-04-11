package com.parallel.learn.redis.web.dto;

public record RateLimitRequest(String rateLimiterName, long permitsPerSecond) {
}
