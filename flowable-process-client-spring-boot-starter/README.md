# flowable-process-client-spring-boot-starter

`flowable-process-client-spring-boot-starter` 是业务模块接入“集中式流程服务”的客户端 starter。

业务系统引入它之后，可以获得两类能力：

- 通过 `RemoteFlowableProcessService` 远程调用流程中心
- 自动暴露统一回调接口，接收流程中心的生命周期通知

## 模块职责

- 封装流程中心的远程调用
- 基于服务发现调用流程中心
- 自动把业务服务的回调信息注册给流程中心
- 自动暴露统一业务回调入口
- 把回调事件分发给本地 `ProcessLifecycleCallback`

## 核心类

### 自动装配

- `com.parallel.learn.flowable.client.starter.FlowableProcessClientAutoConfiguration`
  - 注册 `RestTemplate`
  - 注册 `ProcessCallbackDispatcher`
  - 注册 `RemoteFlowableProcessService`
  - 注册统一回调控制器

### 配置项

- `com.parallel.learn.flowable.client.starter.FlowableProcessClientProperties`

### 远程服务封装

- `com.parallel.learn.flowable.client.starter.RemoteFlowableProcessService`
  - 对业务代码暴露统一接口
- `com.parallel.learn.flowable.client.starter.DefaultRemoteFlowableProcessService`
  - 使用 `RestTemplate + LoadBalancer + Nacos` 访问流程中心

### 回调入口

- `com.parallel.learn.flowable.client.starter.BusinessProcessCallbackController`
  - 接收流程中心推送过来的流程生命周期事件
  - 转成 `ProcessCallbackContext`
  - 交给 `ProcessCallbackDispatcher`

## 核心设计

### 1. 业务模块不直接依赖 Flowable 引擎

业务模块只拿到一个远程服务：

`RemoteFlowableProcessService`

它对外提供：

- `startProcess`
- `queryTasks`
- `completeTask`

### 2. 回调地址不硬编码

业务模块在调用 `startProcess` 时，starter 会自动把以下信息注册给流程中心：

- 当前业务服务名 `spring.application.name`
- 回调路径 `parallel.flowable.client.callback-path`

流程中心后续会根据这两个信息，通过 Nacos 找到业务服务实例并发起回调。

### 3. 业务扩展仍然通过本地 Callback 实现

业务方依然实现：

`ProcessLifecycleCallback`

只是执行时机变成：

- 流程中心触发事件
- 流程中心回调业务服务统一接口
- starter 将回调再分发到本地 Callback Bean

## 配置项

前缀：`parallel.flowable.client`

```yaml
parallel:
  flowable:
    client:
      process-service-id: flowable-process-center
      base-path: /api/remote/process
      callback-path: /internal/flowable/callback
      register-callback: true
```

### 配置说明

- `process-service-id`
  - 流程中心在注册中心中的服务名
- `base-path`
  - 流程中心接口基础路径
- `callback-path`
  - 当前业务服务用于接收流程回调的统一路径
- `register-callback`
  - 是否在启动流程时自动附带回调注册信息

## 接入方式

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.parallel.learn</groupId>
    <artifactId>flowable-process-client-spring-boot-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 配置 Nacos

```yaml
spring:
  application:
    name: flowable-process-demo
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
```

### 3. 编写流程回调

```java
@Component
public class LeaveApprovalProcessCallback implements ProcessLifecycleCallback {

    @Override
    public String processDefinitionKey() {
        return "leaveApproval";
    }
}
```

### 4. 调用流程中心

```java
@RestController
public class DemoController {

    private final RemoteFlowableProcessService remoteFlowableProcessService;

    public DemoController(RemoteFlowableProcessService remoteFlowableProcessService) {
        this.remoteFlowableProcessService = remoteFlowableProcessService;
    }
}
```

## 运行流程

1. 业务模块调用 `RemoteFlowableProcessService.startProcess`
2. starter 自动附带 `serviceId` 和 `callbackPath`
3. 流程中心启动流程
4. 流程中心产生流程事件
5. 流程中心通过 Nacos 找到业务服务实例
6. 流程中心回调业务服务 `/internal/flowable/callback`
7. starter 分发回调到本地 `ProcessLifecycleCallback`

## 优点

- 业务模块无需内嵌 Flowable 引擎
- 回调目标动态发现，不写死
- 业务侧接入体验接近本地服务调用
- 与集中式流程治理方案天然兼容

## 适用场景

- 公司内多个业务服务共享一套流程中心
- 流程定义和引擎希望统一治理
- 业务模块只负责流程发起和业务回调处理
