package com.parallel.learn.redis.web.dto;

import java.util.List;

public record HyperLogLogRequest(String key, List<String> values) {
}
