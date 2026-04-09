# flowable-process-demo

`flowable-process-demo` 是集中式 Flowable 方案中的业务模块示例。

它本身不内嵌 Flowable 引擎，而是通过 `flowable-process-client-spring-boot-starter` 调用独立的流程中心，并接收流程回调。

## 模块职责

- 演示业务系统如何调用集中式流程服务
- 演示业务系统如何实现流程生命周期回调
- 演示业务系统如何通过 Nacos 被流程中心动态发现

## 依赖关系

当前 demo 依赖：

- `flowable-process-client-spring-boot-starter`
- `spring-boot-starter-web`

它不再依赖本地 Flowable 引擎，也不再本地部署 BPMN XML。

## 核心类

### 启动类

- `com.parallel.learn.flowable.demo.FlowableProcessDemoApplication`

### 业务接口

- `com.parallel.learn.flowable.demo.controller.ProcessController`
  - 对外提供演示接口
  - 内部调用 `RemoteFlowableProcessService`

### 业务回调

- `com.parallel.learn.flowable.demo.callback.LeaveApprovalProcessCallback`
  - 监听 `leaveApproval` 这个流程定义的生命周期事件
  - 收到流程中心回调后打印业务日志

### 请求对象

- `com.parallel.learn.flowable.demo.model.StartProcessRequest`
- `com.parallel.learn.flowable.demo.model.CompleteTaskRequest`

## 配置说明

```yaml
server:
  port: 8090

spring:
  application:
    name: flowable-process-demo
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848

parallel:
  flowable:
    client:
      process-service-id: flowable-process-center
      base-path: /api/remote/process
      callback-path: /internal/flowable/callback
      register-callback: true
```

### 配置含义

- `spring.application.name`
  - 业务服务在 Nacos 中的服务名
- `process-service-id`
  - 流程中心服务名
- `callback-path`
  - 流程中心回调业务模块时调用的统一入口

## 启动顺序

### 1. 确保本地 Nacos 已启动

默认地址：

`127.0.0.1:8848`

### 2. 启动流程中心

```bash
mvn -pl flowable-process-center -am spring-boot:run
```

### 3. 启动业务 demo

```bash
mvn -pl flowable-process-demo -am spring-boot:run
```

## 调用示例

### 启动流程

```bash
curl -X POST http://localhost:8090/process/start \
  -H "Content-Type: application/json" \
  -d "{\"processDefinitionKey\":\"leaveApproval\",\"businessKey\":\"LEAVE-1001\",\"variables\":{\"manager\":\"manager001\",\"days\":3}}"
```

说明：

- 请求发给业务模块
- 业务模块通过 starter 转发给流程中心
- starter 自动附带回调注册信息

### 查询任务

```bash
curl "http://localhost:8090/process/{processInstanceId}/tasks?assignee=manager001"
```

### 完成任务

```bash
curl -X POST http://localhost:8090/process/task/{taskId}/complete \
  -H "Content-Type: application/json" \
  -d "{\"variables\":{\"approved\":true}}"
```

## 回调执行流程

1. 业务模块请求流程中心启动流程
2. starter 自动注册当前业务服务名和回调路径
3. 流程中心执行流程
4. 当流程启动、创建任务、完成任务、流程结束时
5. 流程中心通过 Nacos 找到 `flowable-process-demo`
6. 流程中心调用 `/internal/flowable/callback`
7. starter 把回调分发给 `LeaveApprovalProcessCallback`

## 当前示例里的业务回调

`LeaveApprovalProcessCallback` 实现了以下回调：

- `onProcessStarted`
- `onTaskCreated`
- `onTaskCompleted`
- `onProcessCompleted`

这样在演示时，可以很清楚地看到流程中心和业务模块之间的联动。

## 适合拿来做什么

- 本地联调集中式流程服务方案
- 演示业务模块的最小接入方式
- 给其他业务团队做接入模板

## 与旧方案的区别

旧方案里：

- 业务模块本地集成 Flowable starter
- BPMN 和引擎都在业务服务内部

新方案里：

- Flowable 由 `flowable-process-center` 统一托管
- 业务模块只通过 client starter 远程调用
- 业务回调通过 Nacos 动态发现，不写死地址
