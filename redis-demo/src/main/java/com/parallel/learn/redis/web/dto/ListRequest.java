package com.parallel.learn.redis.web.dto;

import java.util.List;

public record ListRequest(String key, List<String> values) {
}
