package com.parallel.learn.rocketmq4;

import com.parallel.learn.rocketmq4.config.DemoRocketMqProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DemoRocketMqProperties.class)
public class RocketMq4DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RocketMq4DemoApplication.class, args);
    }
}
