package com.parallel.learn.redis.web.dto;

import java.util.Map;

public record HashRequest(String key, Map<String, Object> values) {
}
