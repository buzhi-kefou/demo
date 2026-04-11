package com.parallel.learn.jvm.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 线程死锁演示
 * 用于演示线程死锁场景及排查方法
 */
@Slf4j
@RestController
@RequestMapping("/jvm/deadlock")
public class DeadlockDemo {

    // 创建两个锁对象
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();

    // 创建两个可重入锁
    private final Lock reentrantLock1 = new ReentrantLock();
    private final Lock reentrantLock2 = new ReentrantLock();

    /**
     * 死锁示例1：简单的死锁场景
     * 两个线程以不同的顺序获取锁，导致死锁
     */
    @GetMapping("/simple")
    public String simpleDeadlock() {
        log.info("开始演示简单死锁场景");

        // 线程1：先获取lock1，再获取lock2
        Thread thread1 = new Thread(() -> {
            synchronized (lock1) {
                log.info("线程1获取了lock1");
                try {
                    // 模拟处理一些业务逻辑
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("线程1被中断", e);
                }

                log.info("线程1尝试获取lock2");
                synchronized (lock2) {
                    log.info("线程1获取了lock2");
                }
            }
            log.info("线程1释放了所有锁");
        }, "Deadlock-Thread-1");

        // 线程2：先获取lock2，再获取lock1
        Thread thread2 = new Thread(() -> {
            synchronized (lock2) {
                log.info("线程2获取了lock2");
                try {
                    // 模拟处理一些业务逻辑
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("线程2被中断", e);
                }

                log.info("线程2尝试获取lock1");
                synchronized (lock1) {
                    log.info("线程2获取了lock1");
                }
            }
            log.info("线程2释放了所有锁");
        }, "Deadlock-Thread-2");

        // 启动两个线程
        thread1.start();
        thread2.start();

        return "已启动两个线程，它们可能会发生死锁。请使用jstack命令查看线程状态。";
    }

    /**
     * 死锁示例2：使用ReentrantLock导致的死锁
     */
    @GetMapping("/reentrant")
    public String reentrantLockDeadlock() {
        log.info("开始演示ReentrantLock死锁场景");

        // 线程1：先获取reentrantLock1，再获取reentrantLock2
        Thread thread1 = new Thread(() -> {
            try {
                if (reentrantLock1.tryLock(1, TimeUnit.SECONDS)) {
                    log.info("线程1获取了reentrantLock1");
                    try {
                        // 模拟处理一些业务逻辑
                        TimeUnit.MILLISECONDS.sleep(100);

                        log.info("线程1尝试获取reentrantLock2");
                        if (reentrantLock2.tryLock(1, TimeUnit.SECONDS)) {
                            try {
                                log.info("线程1获取了reentrantLock2");
                            } finally {
                                reentrantLock2.unlock();
                                log.info("线程1释放了reentrantLock2");
                            }
                        } else {
                            log.warn("线程1获取reentrantLock2超时");
                        }
                    } finally {
                        reentrantLock1.unlock();
                        log.info("线程1释放了reentrantLock1");
                    }
                } else {
                    log.warn("线程1获取reentrantLock1超时");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("线程1被中断", e);
            }
        }, "ReentrantLock-Thread-1");

        // 线程2：先获取reentrantLock2，再获取reentrantLock1
        Thread thread2 = new Thread(() -> {
            try {
                if (reentrantLock2.tryLock(1, TimeUnit.SECONDS)) {
                    log.info("线程2获取了reentrantLock2");
                    try {
                        // 模拟处理一些业务逻辑
                        TimeUnit.MILLISECONDS.sleep(100);

                        log.info("线程2尝试获取reentrantLock1");
                        if (reentrantLock1.tryLock(1, TimeUnit.SECONDS)) {
                            try {
                                log.info("线程2获取了reentrantLock1");
                            } finally {
                                reentrantLock1.unlock();
                                log.info("线程2释放了reentrantLock1");
                            }
                        } else {
                            log.warn("线程2获取reentrantLock1超时");
                        }
                    } finally {
                        reentrantLock2.unlock();
                        log.info("线程2释放了reentrantLock2");
                    }
                } else {
                    log.warn("线程2获取reentrantLock2超时");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("线程2被中断", e);
            }
        }, "ReentrantLock-Thread-2");

        // 启动两个线程
        thread1.start();
        thread2.start();

        return "已启动两个线程，使用ReentrantLock可能导致死锁。请使用jstack命令查看线程状态。";
    }

    /**
     * 死锁示例3：多个线程之间的循环等待
     */
    @GetMapping("/circular")
    public String circularWaitDeadlock(@RequestParam(defaultValue = "3") int threadCount) {
        log.info("开始演示循环等待死锁场景，线程数: {}", threadCount);

        // 创建多个锁对象
        Object[] locks = new Object[threadCount];
        for (int i = 0; i < threadCount; i++) {
            locks[i] = new Object();
        }

        // 创建多个线程，每个线程以不同的顺序获取锁
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            Thread thread = new Thread(() -> {
                synchronized (locks[threadIndex]) {
                    log.info("线程 {} 获取了锁 {}", threadIndex, threadIndex);
                    try {
                        // 模拟处理一些业务逻辑
                        TimeUnit.MILLISECONDS.sleep(100);

                        int nextLockIndex = (threadIndex + 1) % threadCount;
                        log.info("线程 {} 尝试获取锁 {}", threadIndex, nextLockIndex);
                        synchronized (locks[nextLockIndex]) {
                            log.info("线程 {} 获取了锁 {}", threadIndex, nextLockIndex);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("线程 {} 被中断", threadIndex, e);
                    }
                }
                log.info("线程 {} 释放了所有锁", threadIndex);
            }, "Circular-Thread-" + i);

            thread.start();
        }

        return String.format("已启动 %d 个线程，它们可能会发生循环等待死锁。请使用jstack命令查看线程状态。", threadCount);
    }

    /**
     * 死锁示例4：嵌套锁导致的死锁
     */
    @GetMapping("/nested")
    public String nestedLockDeadlock() {
        log.info("开始演示嵌套锁死锁场景");

        // 创建一个共享资源
        SharedResource resource = new SharedResource();

        // 线程1：先获取resource的锁，再调用method1
        Thread thread1 = new Thread(() -> {
            synchronized (resource) {
                log.info("线程1获取了resource的锁");
                try {
                    // 模拟处理一些业务逻辑
                    TimeUnit.MILLISECONDS.sleep(100);

                    log.info("线程1调用method1");
                    resource.method1();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("线程1被中断", e);
                }
            }
            log.info("线程1释放了resource的锁");
        }, "Nested-Thread-1");

        // 线程2：先调用method2，再获取resource的锁
        Thread thread2 = new Thread(() -> {
            log.info("线程2调用method2");
            try {
                resource.method2();
            } catch (InterruptedException e) {
                log.error("线程2调用method2时发生异常", e);
                throw new RuntimeException(e);
            }

            synchronized (resource) {
                log.info("线程2获取了resource的锁");
            }
            log.info("线程2释放了resource的锁");
        }, "Nested-Thread-2");

        // 启动两个线程
        thread1.start();
        thread2.start();

        return "已启动两个线程，嵌套锁可能导致死锁。请使用jstack命令查看线程状态。";
    }

    /**
     * 共享资源类，包含嵌套锁方法
     */
    class SharedResource {
        public synchronized void method1() throws InterruptedException {
            log.info("进入method1");
            // 模拟处理一些业务逻辑
            TimeUnit.MILLISECONDS.sleep(100);
            log.info("退出method1");
        }

        public synchronized void method2() throws InterruptedException {
            log.info("进入method2");
            // 模拟处理一些业务逻辑
            TimeUnit.MILLISECONDS.sleep(100);
            log.info("退出method2");
        }
    }
}
