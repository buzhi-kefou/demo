package com.parallel.learn.redis.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        String host = redisProperties.getHost();
        int port = redisProperties.getPort();
        int database = redisProperties.getDatabase();
        String address = "redis://" + host + ":" + port;
        singleServerConfig.setAddress(address);
        singleServerConfig.setDatabase(database);
        if (redisProperties.getPassword() != null && !redisProperties.getPassword().isEmpty()) {
            singleServerConfig.setPassword(redisProperties.getPassword());
        }
        return Redisson.create(config);
    }
}
