package com.parallel.learn.rabbitmq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RabbitMQ学习演示应用启动类
 */
@SpringBootApplication
public class RabbitMQDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RabbitMQDemoApplication.class, args);
        System.out.println("RabbitMQ Demo应用启动成功!");
    }
}
