package com.parallel.learn.sentinel.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SentinelDemoProperties.class)
public class SentinelDemoConfiguration {
}
