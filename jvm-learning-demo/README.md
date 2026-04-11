# JVM学习Demo

这个模块提供了学习JVM内部机制、GC日志分析和性能调优的示例代码。

## 功能特性

- 内存分析演示
- GC行为分析
- 类加载器机制演示
- GC日志生成与分析
- 线程死锁演示与排查

## 快速开始

### 1. 构建项目

```bash
mvn clean install
```

### 2. 运行应用

```bash
cd jvm-learning-demo
mvn spring-boot:run
```

### 3. 访问应用

应用启动后，可以通过以下URL访问各个演示功能：

- 内存分析: http://localhost:8081/jvm/memory
- GC分析: http://localhost:8081/jvm/gc
- 类加载器: http://localhost:8081/jvm/classloader

## JVM参数配置

### 生成GC日志

为了生成GC日志，可以在启动应用时添加以下JVM参数：

```bash
java -Xms256m -Xmx512m      -Xlog:gc*:file=gc.log:time,uptime:filecount=5,filesize=10m      -XX:+PrintGCDetails      -XX:+PrintGCDateStamps      -jar jvm-learning-demo.jar
```

### 使用G1垃圾收集器

```bash
java -Xms256m -Xmx512m      -XX:+UseG1GC      -XX:MaxGCPauseMillis=200      -Xlog:gc*:file=gc-g1.log:time,uptime:filecount=5,filesize=10m      -jar jvm-learning-demo.jar
```

### 使用Parallel垃圾收集器

```bash
java -Xms256m -Xmx512m      -XX:+UseParallelGC      -Xlog:gc*:file=gc-parallel.log:time,uptime:filecount=5,filesize=10m      -jar jvm-learning-demo.jar
```

### 使用CMS垃圾收集器

```bash
java -Xms256m -Xmx512m      -XX:+UseConcMarkSweepGC      -Xlog:gc*:file=gc-cms.log:time,uptime:filecount=5,filesize=10m      -jar jvm-learning-demo.jar
```

## GC日志分析工具

### 1. 使用GCViewer

GCViewer是一个开源的GC日志分析工具，可以可视化GC日志：

```bash
java -jar gcviewer.jar gc.log
```

### 2. 使用JDK自带的jstat

查看堆内存使用情况：

```bash
jstat -gc <pid> 1s 10
```

### 3. 使用jmap

生成堆转储文件：

```bash
jmap -dump:format=b,file=heap.hprof <pid>
```

### 4. 使用jvisualvm

JDK自带的监控工具，可以监控内存、线程、GC等：

```bash
jvisualvm
```

## 示例API

### 内存分析API

1. **内存泄漏演示**
   ```
   GET /jvm/memory/leak?size=100000
   ```

2. **正常内存使用演示**
   ```
   GET /jvm/memory/normal?iterations=10000
   ```

3. **查看堆内存信息**
   ```
   GET /jvm/memory/heap-info
   ```

### GC分析API

1. **触发Young GC**
   ```
   GET /jvm/gc/young-gc?objectCount=100000
   ```

2. **触发Full GC**
   ```
   GET /jvm/gc/full-gc?iterations=1000
   ```

3. **对象晋升演示**
   ```
   GET /jvm/gc/promotion?iterations=10000
   ```

4. **GC停顿分析**
   ```
   GET /jvm/gc/pause?objectCount=50000
   ```

### 类加载器API

1. **获取类加载器层次结构**
   ```
   GET /jvm/classloader/hierarchy
   ```

2. **自定义类加载器演示**
   ```
   GET /jvm/classloader/custom
   ```

### 死锁排查API

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

## 学习资源

- [JVM官方文档](https://docs.oracle.com/javase/specs/jvms/se8/html/)
- [GC调优指南](https://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/)
- [GC日志分析](https://blogs.oracle.com/java-platform-group/g1-gc-log-analysis)

## 注意事项

- 在生产环境中使用不同的JVM参数前，请先在测试环境中充分测试
- 分析GC日志时，关注Full GC的频率和停顿时间
- 内存泄漏问题需要结合堆转储文件进行分析
- 死锁排查可以使用jstack、jconsole、jvisualvm等工具
- 详细的死锁排查方法请参考 [DEADLOCK_TROUBLESHOOTING.md](DEADLOCK_TROUBLESHOOTING.md)
