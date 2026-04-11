package com.parallel.learn.jvm.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 内存分析演示
 * 用于演示JVM内存分配和GC行为
 */
@Slf4j
@RestController
@RequestMapping("/jvm/memory")
public class MemoryAnalysisDemo {

    /**
     * 演示内存泄漏场景
     * 创建大量对象并保留引用，导致内存无法回收
     */
    @GetMapping("/leak")
    public String memoryLeak(@RequestParam(defaultValue = "100000") int size) {
        List<byte[]> memoryLeakList = new ArrayList<>();

        try {
            for (int i = 0; i < size; i++) {
                // 每次分配1MB内存
                byte[] bytes = new byte[1024 * 1024];
                memoryLeakList.add(bytes);

                if (i % 100 == 0) {
                    log.info("已分配 {} MB 内存", i);
                }
            }
            return "成功分配 " + size + " MB 内存，当前列表大小: " + memoryLeakList.size();
        } catch (OutOfMemoryError e) {
            log.error("内存溢出: ", e);
            return "内存溢出，已分配 " + memoryLeakList.size() + " MB 内存";
        }
    }

    /**
     * 演示正常内存使用场景
     * 创建对象后允许GC回收
     */
    @GetMapping("/normal")
    public String normalMemoryUsage(@RequestParam(defaultValue = "10000") int iterations) {
        for (int i = 0; i < iterations; i++) {
            // 创建临时对象，方法结束后可以被GC回收
            byte[] tempBytes = new byte[1024 * 1024];

            if (i % 1000 == 0) {
                log.info("执行第 {} 次迭代", i);
            }
        }
        return "完成 " + iterations + " 次迭代，对象已创建并等待GC回收";
    }

    /**
     * 演示堆内存信息
     */
    @GetMapping("/heap-info")
    public String getHeapInfo() {
        Runtime runtime = Runtime.getRuntime();

        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;

        return String.format(
            "堆内存信息 - 最大: %d MB, 已分配: %d MB, 已使用: %d MB, 空闲: %d MB",
            maxMemory, totalMemory, usedMemory, freeMemory
        );
    }
}
