# flowable-process-center

`flowable-process-center` 是集中式流程服务模块。

它是当前 Flowable 新方案里的核心服务，负责统一托管 Flowable 引擎、统一部署流程定义、统一提供流程接口，并在流程执行过程中动态回调业务模块。

## 模块职责

- 作为独立服务启动 Flowable 引擎
- 自动部署 BPMN XML
- 对外提供统一流程 API
- 托管流程实例和任务状态
- 监听流程生命周期事件
- 根据流程变量中的回调注册信息动态回调业务系统
- 通过 Nacos 做业务服务发现

## 核心类

### 启动入口

- `com.parallel.learn.flowable.center.FlowableProcessCenterApplication`

### 配置装配

- `com.parallel.learn.flowable.center.config.ProcessCenterConfiguration`
  - 注册 `FlowableProcessService`
  - 注册 XML 自动部署 Runner
  - 注册回调通知器
  - 注册流程事件监听器
  - 注册远程流程服务
- `com.parallel.learn.flowable.center.config.ProcessCenterProperties`
  - 绑定中心服务配置

### 对外接口

- `com.parallel.learn.flowable.center.controller.ProcessCenterController`
  - `POST /api/remote/process`
  - `GET /api/remote/process/{processInstanceId}/tasks`
  - `POST /api/remote/process/task/{taskId}/complete`

### 业务编排服务

- `com.parallel.learn.flowable.center.service.ProcessCenterRemoteService`
  - 负责把远程请求转成 `FlowableProcessService` 调用
  - 负责将回调注册信息写入流程变量

### 回调通知

- `com.parallel.learn.flowable.center.listener.RemoteProcessEventListener`
  - 监听 Flowable 流程事件
- `com.parallel.learn.flowable.center.callback.BusinessCallbackNotifier`
  - 解析流程变量中的回调目标
  - 使用 Nacos 发现业务模块实例
  - 通过 HTTP 推送生命周期回调

## 回调不硬编码的实现方式

这是这个模块最关键的设计点。

### 启动流程时

业务模块会传入：

- `callbackRegistration.serviceId`
- `callbackRegistration.callbackPath`

流程中心收到后，会把它们写入流程变量：

- `_callbackServiceId`
- `_callbackPath`

### 流程执行时

当流程触发以下事件时：

- `PROCESS_STARTED`
- `TASK_CREATED`
- `TASK_COMPLETED`
- `PROCESS_COMPLETED`
- `PROCESS_CANCELLED`

流程中心会：

1. 从运行时变量或历史变量中读取 `_callbackServiceId` 和 `_callbackPath`
2. 通过 Nacos 查找对应业务服务实例
3. 向该业务服务回调统一接口

所以流程中心不需要知道具体业务模块类名、Bean 名或固定 URL。

## 对外 REST 接口

### 启动流程

`POST /api/remote/process`

请求体示例：

```json
{
  "processDefinitionKey": "leaveApproval",
  "businessKey": "LEAVE-1001",
  "variables": {
    "manager": "manager001",
    "days": 3
  },
  "callbackRegistration": {
    "serviceId": "flowable-process-demo",
    "callbackPath": "/internal/flowable/callback"
  }
}
```

### 查询任务

`GET /api/remote/process/{processInstanceId}/tasks?assignee=manager001`

### 完成任务

`POST /api/remote/process/task/{taskId}/complete`

```json
{
  "variables": {
    "approved": true
  }
}
```

## 配置项

### 基础配置

```yaml
server:
  port: 8091

spring:
  application:
    name: flowable-process-center
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
```

### Flowable 配置

```yaml
flowable:
  async-executor-activate: false
  database-schema-update: true
  check-process-definitions: false
```

### 中心服务自定义配置

```yaml
parallel:
  flowable:
    center:
      deployment:
        auto-deploy-enabled: true
        deployment-name: company-shared-flowable-center
        resource-locations:
          - classpath*:/processes/**/*.bpmn20.xml
      logging:
        detailed-event-log-enabled: true
```

## BPMN 部署方式

当前模块内置了示例流程：

- `src/main/resources/processes/leave-approval.bpmn20.xml`

应用启动后由 `FlowableXmlDeploymentRunner` 自动部署。

## 启动方式

```bash
mvn -pl flowable-process-center -am spring-boot:run
```

## 在当前架构中的位置

- 业务模块只跟 client starter 交互
- client starter 通过 Nacos 找到流程中心
- 流程中心统一托管 Flowable
- 流程中心再通过 Nacos 回调业务模块

## 适用场景

- 多业务线共享一套流程能力
- 流程定义集中治理
- 流程引擎统一运维
- 需要跨服务流程编排，但业务回调逻辑仍留在各业务系统内
