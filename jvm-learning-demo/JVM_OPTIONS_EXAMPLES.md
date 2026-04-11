# JVM参数配置示例

本文档提供了不同场景下的JVM参数配置示例，供学习和参考。

## 基础内存配置

```bash
# 设置堆内存初始大小为256MB，最大为512MB
-Xms256m -Xmx512m

# 设置新生代大小为128MB
-Xmn128m

# 设置新生代与老年代比例
-XX:NewRatio=2

# 设置Eden区与Survivor区比例
-XX:SurvivorRatio=8
```

## GC日志配置

### JDK 9及以上版本

```bash
# 开启GC日志，记录所有GC事件
-Xlog:gc*:file=gc.log:time,uptime:filecount=5,filesize=10m

# 只记录GC事件和堆内存变化
-Xlog:gc+heap=debug:file=gc-heap.log:time,uptime:filecount=5,filesize=10m

# 记录GC停顿时间
-Xlog:gc+cpu=info:file=gc-cpu.log:time,uptime:filecount=5,filesize=10m
```

### JDK 8及以下版本

```bash
# 开启GC日志
-XX:+PrintGCDetails
-XX:+PrintGCDateStamps
-Xloggc:gc.log

# 记录GC停顿时间
-XX:+PrintGCApplicationStoppedTime

# 记录堆内存变化
-XX:+PrintHeapAtGC
```

## 垃圾收集器配置

### G1垃圾收集器 (推荐)

```bash
# 使用G1垃圾收集器
-XX:+UseG1GC

# 设置最大GC停顿时间为200毫秒
-XX:MaxGCPauseMillis=200

# 设置并行GC线程数为4
-XX:ParallelGCThreads=4

# 设置并发GC线程数为2
-XX:ConcGCThreads=2

# G1 Region大小设置为16MB
-XX:G1HeapRegionSize=16m

# GC日志配置
-Xlog:gc*:file=gc-g1.log:time,uptime:filecount=5,filesize=10m
```

### Parallel垃圾收集器 (高吞吐量)

```bash
# 使用Parallel垃圾收集器
-XX:+UseParallelGC

# 设置并行GC线程数为4
-XX:ParallelGCThreads=4

# 设置最大GC停顿时间目标
-XX:MaxGCPauseMillis=200

# 设置吞吐量目标为90%
-XX:GCTimeRatio=9

# GC日志配置
-Xlog:gc*:file=gc-parallel.log:time,uptime:filecount=5,filesize=10m
```

### CMS垃圾收集器 (低延迟)

```bash
# 使用CMS垃圾收集器
-XX:+UseConcMarkSweepGC

# 设置并行GC线程数为4
-XX:ParallelGCThreads=4

# 设置并发GC线程数为2
-XX:ConcGCThreads=2

# 设置老年代使用比例达到70%时触发CMS
-XX:CMSInitiatingOccupancyFraction=70

# GC日志配置
-Xlog:gc*:file=gc-cms.log:time,uptime:filecount=5,filesize=10m
```

## 性能调优参数

```bash
# 禁用偏向锁
-XX:-UseBiasedLocking

# 启用压缩指针
-XX:+UseCompressedOops

# 启用压缩类指针
-XX:+UseCompressedClassPointers

# 设置栈大小为256KB
-Xss256k

# 启用大内存页
-XX:+UseLargePages

# 设置元空间初始大小为128MB，最大为256MB
-XX:MetaspaceSize=128m
-XX:MaxMetaspaceSize=256m
```

## 监控与诊断参数

```bash
# 启用JMX远程监控
-Dcom.sun.management.jmxremote
-Dcom.sun.management.jmxremote.port=9010
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.ssl=false

# 启用Heap Dump
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/path/to/dumps/

# 启用类加载统计
-XX:+TraceClassLoading
-XX:+TraceClassUnloading

# 启用字节码验证
-XX:+VerifyBeforeGC
-XX:+VerifyAfterGC
```

## 应用启动示例

### 使用G1垃圾收集器启动应用

```bash
java -Xms512m -Xmx1024m      -XX:+UseG1GC      -XX:MaxGCPauseMillis=200      -XX:ParallelGCThreads=4      -XX:ConcGCThreads=2      -Xlog:gc*:file=gc.log:time,uptime:filecount=5,filesize=10m      -jar jvm-learning-demo.jar
```

### 使用Parallel垃圾收集器启动应用

```bash
java -Xms512m -Xmx1024m      -XX:+UseParallelGC      -XX:ParallelGCThreads=4      -XX:MaxGCPauseMillis=200      -Xlog:gc*:file=gc.log:time,uptime:filecount=5,filesize=10m      -jar jvm-learning-demo.jar
```

### 使用CMS垃圾收集器启动应用

```bash
java -Xms512m -Xmx1024m      -XX:+UseConcMarkSweepGC      -XX:ParallelGCThreads=4      -XX:ConcGCThreads=2      -XX:CMSInitiatingOccupancyFraction=70      -Xlog:gc*:file=gc.log:time,uptime:filecount=5,filesize=10m      -jar jvm-learning-demo.jar
```

## 参数选择建议

1. **内存大小**
   - 初始堆大小(-Xms)和最大堆大小(-Xmx)通常设置为相同值，避免运行时动态调整带来的性能损耗
   - 新生代大小(-Xmn)通常设置为堆大小的1/3到1/2

2. **垃圾收集器选择**
   - G1垃圾收集器：适用于大多数场景，特别是大内存应用
   - Parallel垃圾收集器：适用于吞吐量优先的场景
   - CMS垃圾收集器：适用于低延迟要求的场景

3. **GC日志**
   - 生产环境建议开启GC日志，便于问题排查
   - 日志文件大小和数量应根据磁盘空间和保留周期合理设置

4. **监控参数**
   - 生产环境建议开启JMX远程监控
   - 建议开启Heap Dump，便于内存溢出问题分析
