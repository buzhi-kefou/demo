# 线程死锁排查指南

本文档介绍如何排查Java应用程序中的线程死锁问题。

## 什么是死锁

死锁是指两个或两个以上的线程在执行过程中，因争夺资源而造成的一种互相等待的现象，若无外力作用，它们都将无法推进下去。

## 死锁的四个必要条件

1. **互斥条件**：资源不能被共享，只能由一个线程使用。
2. **请求与保持条件**：一个线程因请求资源而阻塞时，对已获得的资源保持不放。
3. **不剥夺条件**：线程已获得的资源，在未使用完之前，不能被强行剥夺。
4. **循环等待条件**：若干线程之间形成一种头尾相接的循环等待资源关系。

只要这四个条件中有一个不满足，死锁就不会发生。

## 死锁示例

本模块提供了四种死锁示例，可以通过以下API触发：

1. **简单死锁场景**
   ```
   GET /jvm/deadlock/simple
   ```

2. **ReentrantLock死锁场景**
   ```
   GET /jvm/deadlock/reentrant
   ```

3. **循环等待死锁场景**
   ```
   GET /jvm/deadlock/circular?threadCount=3
   ```

4. **嵌套锁死锁场景**
   ```
   GET /jvm/deadlock/nested
   ```

## 死锁排查方法

### 1. 使用jstack命令

jstack是JDK自带的命令行工具，可以打印Java线程的堆栈跟踪信息，包括死锁检测。

#### 步骤：

1. 找到Java进程ID：
   ```bash
   jps -l
   ```

2. 使用jstack生成线程转储：
   ```bash
   jstack <pid> > thread_dump.txt
   ```

3. 查看线程转储文件，查找"Found one Java-level deadlock"或类似的死锁信息。

#### 示例输出：

```
Found one Java-level deadlock:
=============================
"Deadlock-Thread-1":
  waiting to lock monitor 0x00007f8568006ae8 (object 0x00000006c3a8f8f8, a java.lang.Object),
  which is held by "Deadlock-Thread-2"
"Deadlock-Thread-2":
  waiting to lock monitor 0x00007f8568007a98 (object 0x00000006c3a8f8e8, a java.lang.Object),
  which is held by "Deadlock-Thread-1"

Java stack information for the threads listed above:
===================================================
"Deadlock-Thread-1":
    at com.parallel.learn.jvm.demo.DeadlockDemo.lambda$simpleDeadlock$0(DeadlockDemo.java:35)
    - waiting to lock <0x00000006c3a8f8f8> (a java.lang.Object)
    - locked <0x00000006c3a8f8e8> (a java.lang.Object)
    at com.parallel.learn.jvm.demo.DeadlockDemo$$Lambda$1/0x0000000800b52840.run(Unknown Source)
    at java.lang.Thread.run(Thread.java:748)
"Deadlock-Thread-2":
    at com.parallel.learn.jvm.demo.DeadlockDemo.lambda$simpleDeadlock$1(DeadlockDemo.java:58)
    - waiting to lock <0x00000006c3a8f8e8> (a java.lang.Object)
    - locked <0x00000006c3a8f8f8> (a java.lang.Object)
    at com.parallel.learn.jvm.demo.DeadlockDemo$$Lambda$2/0x0000000800b52c40.run(Unknown Source)
    at java.lang.Thread.run(Thread.java:748)
```

### 2. 使用jconsole

jconsole是JDK自带的图形化监控工具，可以监控Java应用程序的内存、线程、类加载等情况。

#### 步骤：

1. 启动jconsole：
   ```bash
   jconsole
   ```

2. 在jconsole界面中，选择要监控的Java进程。

3. 点击"线程"标签，查看线程状态。

4. 点击"检测死锁"按钮，jconsole会显示检测到的死锁信息。

### 3. 使用jvisualvm

jvisualvm是JDK自带的图形化监控工具，功能比jconsole更强大。

#### 步骤：

1. 启动jvisualvm：
   ```bash
   jvisualvm
   ```

2. 在jvisualvm界面中，选择要监控的Java进程。

3. 点击"线程"标签，查看线程状态。

4. 在线程视图中，可以查看线程的堆栈跟踪，识别死锁。

### 4. 使用JMX

可以通过JMX API编程方式检测死锁：

```java
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class DeadlockDetector {
    public static void detectDeadlock() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedThreads = threadBean.findDeadlockedThreads();

        if (deadlockedThreads != null && deadlockedThreads.length > 0) {
            System.out.println("检测到死锁，涉及的线程数: " + deadlockedThreads.length);

            for (long threadId : deadlockedThreads) {
                ThreadInfo info = threadBean.getThreadInfo(threadId);
                System.out.println("死锁线程: " + info.getThreadName());
                System.out.println("堆栈跟踪: ");
                for (StackTraceElement element : info.getStackTrace()) {
                    System.out.println("	" + element);
                }
            }
        } else {
            System.out.println("未检测到死锁");
        }
    }
}
```

## 死锁预防与避免

### 1. 破坏死锁的四个必要条件

1. **破坏互斥条件**：尽可能使用共享资源，减少锁的使用。
2. **破坏请求与保持条件**：一次性申请所有需要的资源。
3. **破坏不剥夺条件**：当线程无法获取所需资源时，释放已持有的资源。
4. **破坏循环等待条件**：按固定顺序获取资源。

### 2. 使用锁超时

使用tryLock代替lock，设置超时时间，避免无限等待：

```java
if (lock.tryLock(1, TimeUnit.SECONDS)) {
    try {
        // 执行业务逻辑
    } finally {
        lock.unlock();
    }
} else {
    // 获取锁超时，处理超时逻辑
}
```

### 3. 使用可重入锁

ReentrantLock提供了比synchronized更灵活的锁机制，支持公平锁、可中断锁等特性：

```java
ReentrantLock lock = new ReentrantLock(true); // 公平锁
try {
    lock.lock();
    // 执行业务逻辑
} finally {
    lock.unlock();
}
```

### 4. 避免嵌套锁

尽量避免在一个已经持有锁的方法中调用另一个需要锁的方法，或者确保锁的获取顺序一致。

### 5. 使用并发工具类

Java并发包提供了许多高级并发工具类，如CountDownLatch、CyclicBarrier、Semaphore等，可以减少直接使用锁的需要。

## 实践建议

1. **代码审查**：在代码审查阶段，重点关注锁的使用情况，确保锁的获取顺序一致。
2. **单元测试**：编写并发测试用例，使用压力测试工具模拟高并发场景，检测潜在的死锁问题。
3. **监控告警**：在生产环境中部署死锁监控工具，及时发现和处理死锁问题。
4. **日志记录**：记录锁的获取和释放操作，便于问题排查。
5. **定期检查**：定期检查线程转储，及时发现潜在的死锁问题。

## 学习资源

- [Java并发编程实战](https://book.douban.com/subject/10484692/)
- [Java并发编程的艺术](https://book.douban.com/subject/26591326/)
- [Java官方文档 - 并发](https://docs.oracle.com/javase/tutorial/essential/concurrency/)
