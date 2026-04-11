package com.parallel.learn.jvm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * JVM学习Demo应用
 * 用于学习JVM内部机制、GC日志分析和性能调优
 */
@SpringBootApplication
public class JvmLearningApplication {

    public static void main(String[] args) {
        SpringApplication.run(JvmLearningApplication.class, args);
    }
}
