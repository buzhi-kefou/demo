package com.parallel.learn.redis.web.dto;

public record ZSetRequest(String key, String value, double score) {
}
