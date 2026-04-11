package com.parallel.learn.jvm.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * GC分析演示
 * 用于演示不同GC行为和GC日志分析
 */
@Slf4j
@RestController
@RequestMapping("/jvm/gc")
public class GcAnalysisDemo {

    /**
     * 演示Young GC
     * 创建大量短生命周期对象，触发Young GC
     */
    @GetMapping("/young-gc")
    public String triggerYoungGc(@RequestParam(defaultValue = "100000") int objectCount) {
        log.info("开始创建 {} 个短生命周期对象，触发Young GC", objectCount);

        for (int i = 0; i < objectCount; i++) {
            // 创建临时对象，方法结束后可以被GC回收
            byte[] tempBytes = new byte[1024];

            if (i % 10000 == 0) {
                log.info("已创建 {} 个对象", i);
            }
        }

        return String.format("已创建 %d 个短生命周期对象，Young GC应该已被触发", objectCount);
    }

    /**
     * 演示Full GC
     * 创建大量长生命周期对象，填满老年代，触发Full GC
     */
    @GetMapping("/full-gc")
    public String triggerFullGc(@RequestParam(defaultValue = "1000") int iterations) {
        log.info("开始创建长生命周期对象，可能触发Full GC");

        List<byte[]> longLivedObjects = new ArrayList<>();

        try {
            for (int i = 0; i < iterations; i++) {
                // 创建较大的对象，可能进入老年代
                byte[] largeObject = new byte[1024 * 512]; // 512KB
                longLivedObjects.add(largeObject);

                if (i % 100 == 0) {
                    log.info("已创建 {} 个长生命周期对象", i);
                }
            }
            return String.format("已创建 %d 个长生命周期对象，当前列表大小: %d", 
                                iterations, longLivedObjects.size());
        } catch (OutOfMemoryError e) {
            log.error("内存溢出: ", e);
            return String.format("内存溢出，已创建 %d 个长生命周期对象", longLivedObjects.size());
        }
    }

    /**
     * 演示对象晋升
     * 创建多次存活的对象，观察其从新生代晋升到老年代的过程
     */
    @GetMapping("/promotion")
    public String objectPromotion(@RequestParam(defaultValue = "10000") int iterations) {
        log.info("开始演示对象晋升过程");

        List<byte[]> survivingObjects = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < iterations; i++) {
            // 创建中等大小的对象
            byte[] object = new byte[1024 * 100]; // 100KB

            // 随机决定对象是否存活
            if (random.nextDouble() > 0.7) {
                survivingObjects.add(object);
            }

            // 定期清理部分对象，模拟对象晋升
            if (survivingObjects.size() > 100) {
                survivingObjects.remove(0);
            }

            if (i % 1000 == 0) {
                log.info("已完成 {} 次迭代，当前存活对象数: {}", i, survivingObjects.size());
            }
        }

        return String.format("完成 %d 次迭代，最终存活对象数: %d", iterations, survivingObjects.size());
    }

    /**
     * 演示GC停顿
     * 创建大量对象，观察GC停顿时间
     */
    @GetMapping("/pause")
    public String gcPause(@RequestParam(defaultValue = "50000") int objectCount) {
        log.info("开始创建 {} 个对象，观察GC停顿时间", objectCount);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < objectCount; i++) {
            // 创建临时对象
            byte[] tempBytes = new byte[1024 * 10]; // 10KB

            if (i % 5000 == 0) {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - startTime;
                log.info("已创建 {} 个对象，已耗时 {} ms", i, elapsedTime);
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        return String.format("完成 %d 个对象的创建，总耗时: %d ms", objectCount, totalTime);
    }
}
