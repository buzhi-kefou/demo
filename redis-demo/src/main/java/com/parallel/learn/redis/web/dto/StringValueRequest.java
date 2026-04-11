package com.parallel.learn.redis.web.dto;

public record StringValueRequest(String key, String value, Long ttlSeconds) {
}
