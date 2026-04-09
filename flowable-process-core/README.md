# flowable-process-core

`flowable-process-core` 是 Flowable 能力沉淀模块，负责提供与具体业务解耦的公共流程能力。

## 模块职责

- 封装通用流程服务接口
- 封装流程生命周期回调接口
- 封装回调分发逻辑
- 封装 Flowable 事件监听能力
- 封装 BPMN XML 自动部署能力

这个模块不直接提供完整可运行应用，也不关心 Nacos、远程调用或业务系统地址。

## 核心类

### 流程服务

- `com.parallel.learn.flowable.core.service.FlowableProcessService`
  - 统一定义流程启动、任务查询、任务完成接口
- `com.parallel.learn.flowable.core.service.DefaultFlowableProcessService`
  - 基于 `RuntimeService` 和 `TaskService` 对 Flowable 做二次封装

### 回调机制

- `com.parallel.learn.flowable.core.callback.ProcessLifecycleCallback`
  - 业务侧扩展点
  - 通过 `processDefinitionKey()` 标识自己处理哪个流程
  - 支持以下生命周期方法：
    - `onProcessStarted`
    - `onTaskCreated`
    - `onTaskCompleted`
    - `onProcessCompleted`
    - `onProcessCancelled`
- `com.parallel.learn.flowable.core.callback.ProcessCallbackContext`
  - 回调上下文对象
  - 封装事件类型、流程定义 Key、流程实例 ID、执行 ID、任务 ID、业务 Key、流程变量
- `com.parallel.learn.flowable.core.callback.ProcessCallbackDispatcher`
  - 根据 `processDefinitionKey` 找到对应回调实现并分发事件

### 事件监听

- `com.parallel.learn.flowable.core.logging.FlowableProcessEventListener`
  - 监听 Flowable 引擎事件
  - 把原始事件转换成 `ProcessCallbackContext`
  - 将事件交给 `ProcessCallbackDispatcher`

### 自动部署

- `com.parallel.learn.flowable.core.deployment.FlowableXmlDeploymentRunner`
  - Spring Boot 启动后扫描 BPMN XML 并自动部署
- `com.parallel.learn.flowable.core.deployment.FlowableDeploymentProperties`
  - 自动部署配置
- `com.parallel.learn.flowable.core.logging.FlowableLoggingProperties`
  - 日志开关配置

## 使用方式

这个模块通常不会被业务应用直接单独引入使用，而是作为以下模块的底层依赖：

- `flowable-process-spring-boot-starter`
- `flowable-process-client-spring-boot-starter`
- `flowable-process-center`

## 适用场景

适合沉淀以下共性能力：

- 统一的 Flowable 服务封装
- 统一的流程回调扩展模型
- 统一的 BPMN 自动部署机制
- 统一的事件日志机制

## 设计说明

这个模块刻意不绑定具体业务，不保存业务回调地址，也不依赖注册中心。这样可以保证：

- 可以复用于本地嵌入 Flowable 的模式
- 也可以复用于集中式流程中心模式
- 业务扩展点保持一致

## 在当前工程中的位置

- 本地嵌入 Flowable 模式：由 `flowable-process-spring-boot-starter` 使用
- 集中式流程中心模式：由 `flowable-process-center` 和 `flowable-process-client-spring-boot-starter` 间接使用
