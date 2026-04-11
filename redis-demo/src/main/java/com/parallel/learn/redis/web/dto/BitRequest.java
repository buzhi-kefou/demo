package com.parallel.learn.redis.web.dto;

public record BitRequest(String key, long offset, boolean value) {
}
