# flowable-process-remote-api

`flowable-process-remote-api` 是集中式流程服务方案中的共享协议模块。

它负责定义流程中心和业务模块之间通用的请求、响应和回调报文模型。

## 模块职责

- 定义启动流程请求
- 定义完成任务请求
- 定义流程实例响应
- 定义任务查询响应
- 定义流程生命周期回调请求
- 定义业务回调注册信息

这个模块不包含业务逻辑，也不包含自动装配。

## 核心 DTO

### 启动流程

- `ProcessStartRequest`
  - `processDefinitionKey`
  - `businessKey`
  - `variables`
  - `callbackRegistration`

### 业务回调注册

- `BusinessCallbackRegistration`
  - `serviceId`
  - `callbackPath`

这个对象很关键，它决定了流程中心在流程执行期间应该回调哪个业务服务。

### 完成任务

- `ProcessTaskCompleteRequest`
  - `variables`

### 流程响应

- `ProcessInstanceResponse`
  - `processInstanceId`
  - `processDefinitionId`
  - `processDefinitionKey`
  - `businessKey`
  - `variables`

### 任务响应

- `ProcessTaskResponse`
  - `taskId`
  - `taskName`
  - `assignee`
  - `processInstanceId`
  - `processDefinitionId`

### 生命周期回调

- `ProcessLifecycleEventRequest`
  - `eventType`
  - `processDefinitionKey`
  - `processInstanceId`
  - `executionId`
  - `taskId`
  - `businessKey`
  - `variables`

## 为什么要单独拆模块

把协议单独拆出来有几个好处：

- 流程中心和业务 starter 复用同一套 DTO
- 避免服务两边各自定义一份接口对象
- 降低接口演进时的不一致风险
- 方便以后单独抽成公共 SDK

## 当前调用关系

- `flowable-process-center`
  - 对外暴露基于这些 DTO 的 REST 接口
- `flowable-process-client-spring-boot-starter`
  - 使用这些 DTO 调用流程中心
  - 使用这些 DTO 接收流程中心回调

## 设计原则

- DTO 只表达协议，不放业务逻辑
- 字段尽量直白，不引入业务特定语义
- 兼容流程中心和业务模块两个方向的调用
