# flowable-process-spring-boot-starter

`flowable-process-spring-boot-starter` 是面向“本地嵌入式 Flowable”方案的自动装配模块。

它适合这样的场景：

- 业务应用自己内嵌 Flowable 引擎
- BPMN 流程定义跟业务服务一起部署
- 回调逻辑直接在本地业务 JVM 中执行

如果你要的是“集中式流程服务 + 业务系统远程调用”，优先使用 `flowable-process-client-spring-boot-starter` 和 `flowable-process-center`。

## 模块职责

- 自动注册 `FlowableProcessService`
- 自动注册 `ProcessCallbackDispatcher`
- 自动注册 `FlowableProcessEventListener`
- 自动把监听器挂到 Flowable 引擎
- 自动注册 BPMN XML 部署 Runner

## 核心类

- `com.parallel.learn.flowable.starter.ParallelFlowableAutoConfiguration`
  - 自动装配入口
- `com.parallel.learn.flowable.starter.ParallelFlowableProperties`
  - 对外暴露配置项
- `META-INF/spring.factories`
  - Spring Boot 自动装配声明

## 自动装配内容

应用引入这个 starter 后，Spring Boot 启动时会自动完成以下事情：

1. 创建 `ProcessCallbackDispatcher`
2. 创建 `FlowableProcessService`
3. 创建 `FlowableProcessEventListener`
4. 把 `FlowableProcessEventListener` 注册到 Flowable 引擎
5. 如果开启自动部署，则创建 `FlowableXmlDeploymentRunner`

## 配置项

前缀：`parallel.flowable`

### XML 部署

```yaml
parallel:
  flowable:
    deployment:
      auto-deploy-enabled: true
      deployment-name: company-shared-flowable
      resource-locations:
        - classpath*:/processes/**/*.bpmn20.xml
```

### 事件日志

```yaml
parallel:
  flowable:
    logging:
      detailed-event-log-enabled: true
```

## 业务模块接入方式

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.parallel.learn</groupId>
    <artifactId>flowable-process-spring-boot-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 放置 BPMN 文件

建议放到：

`src/main/resources/processes/`

### 3. 编写回调实现

实现 `ProcessLifecycleCallback` 并声明为 Spring Bean：

```java
@Component
public class LeaveApprovalProcessCallback implements ProcessLifecycleCallback {

    @Override
    public String processDefinitionKey() {
        return "leaveApproval";
    }
}
```

### 4. 注入流程服务

```java
@Autowired
private FlowableProcessService flowableProcessService;
```

## 调用流程

1. 应用启动
2. starter 自动装配 Flowable 公共能力
3. BPMN 文件自动部署
4. 业务代码调用 `FlowableProcessService` 启动流程或完成任务
5. Flowable 产生事件
6. `FlowableProcessEventListener` 捕获事件
7. `ProcessCallbackDispatcher` 分发到对应业务回调

## 优缺点

### 优点

- 接入简单
- 调试成本低
- 本地调用性能高

### 局限

- 每个业务系统都要内嵌 Flowable
- 流程定义和引擎能力分散在各业务服务中
- 不适合统一治理和集中运维

## 在当前工程中的定位

这是保留的“单体 / 本地嵌入式”方案，对照组意义更强。

当前主推的新方案是：

- `flowable-process-center`
- `flowable-process-client-spring-boot-starter`
