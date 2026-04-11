# sentinel-demo

`sentinel-demo` 是一个面向实战学习的 Sentinel 示例模块。

你可以通过真实接口去练下面这些能力：

- 基础流控
- 热点参数限流
- 熔断降级
- `@SentinelResource` 的 `blockHandler` 和 `fallback`
- 手动 API `SphU.entry`

## 模块结构

- `SentinelDemoApplication`
  - Spring Boot 启动入口
- `SentinelDemoController`
  - 提供学习用 HTTP 接口
- `SentinelDemoService`
  - 放置 Sentinel 资源定义和业务逻辑
- `SentinelBlockHandlers`
  - 统一 block 限流处理方法
- `SentinelFallbacks`
  - 统一 fallback 降级兜底方法

## 依赖

模块引入了：

- `spring-boot-starter-web`
- `spring-cloud-starter-alibaba-sentinel`

父工程已经管理好了 Spring Boot 和 Spring Cloud Alibaba 版本。

## 启动

```bash
mvn -pl sentinel-demo -am spring-boot:run
```

默认端口：

- 应用端口：`8088`
- Sentinel 客户端通信端口：`8719`

默认 Dashboard 地址：

- `127.0.0.1:8858`

如果你的 Sentinel Dashboard 不是这个地址，可以改 [`application.yml`](/Users/zhangzhengmu/IdeaProjects/demo/sentinel-demo/src/main/resources/application.yml)。

## 配置

```yaml
spring:
  application:
    name: sentinel-demo
  cloud:
    sentinel:
      eager: true
      transport:
        dashboard: 127.0.0.1:8858
        port: 8719
      web-context-unify: false
```

说明：

- `eager: true`
  - 应用启动时尽早初始化 Sentinel
- `dashboard`
  - Sentinel 控制台地址
- `port`
  - 当前应用与控制台通信端口
- `web-context-unify: false`
  - 便于你在控制台里更清楚地区分资源

## 学习接口

### 1. 基础流控

```bash
curl "http://localhost:8088/sentinel/hello?name=demo"
```

对应资源名：

- `helloResource`

建议在 Dashboard 中给 `helloResource` 配一个 QPS 流控规则，然后连续刷新观察返回。

被限流后会进入：

- `SentinelBlockHandlers.handleHello`

### 2. 热点参数限流

```bash
curl "http://localhost:8088/sentinel/hot?userId=1&skuId=sku-1"
```

对应资源名：

- `hotParamResource`

建议在 Dashboard 中：

1. 新建热点规则
2. 资源名选 `hotParamResource`
3. 参数索引填 `0`
4. 也就是按 `userId` 做热点限流

如果对同一个 `userId` 高频访问，就会看到 block 返回。

### 3. 熔断降级

#### 正常请求

```bash
curl "http://localhost:8088/sentinel/degrade?mode=ok"
```

#### 模拟慢调用

```bash
curl "http://localhost:8088/sentinel/degrade?mode=slow"
```

当前默认慢调用时长由配置控制：

- `demo.sentinel.degrade-delay-ms=800`

可以在 Dashboard 中对资源 `degradeByException` 配置 RT 熔断规则。

#### 模拟异常

```bash
curl "http://localhost:8088/sentinel/degrade?mode=error"
```

这会主动抛出业务异常，并进入：

- `SentinelFallbacks.degradeFallback`

你也可以给 `degradeByException` 配异常比例或异常数熔断规则。

### 4. 手动定义资源

```bash
curl "http://localhost:8088/sentinel/manual?name=manual-user"
```

对应资源名：

- `manualEntryResource`

这个接口不是靠注解，而是直接演示：

```java
SphU.entry("manualEntryResource")
```

适合学习 Sentinel 原生 API 的使用方式。

### 5. 学习提示接口

```bash
curl "http://localhost:8088/sentinel/rules/guide"
```

会返回每个演示接口适合配置哪种规则。

## 推荐学习顺序

### 第一阶段：先认识资源

1. 启动应用
2. 调几次各个接口
3. 打开 Sentinel Dashboard
4. 观察资源是否已上报

### 第二阶段：做基础流控

1. 对 `helloResource` 配 QPS 规则
2. 连续请求 `/sentinel/hello`
3. 观察 blockHandler 返回

### 第三阶段：做热点规则

1. 对 `hotParamResource` 配热点参数规则
2. 固定 `userId` 高频请求
3. 观察热点限流效果

### 第四阶段：做熔断

1. 对 `degradeByException` 配 RT 规则
2. 调 `/sentinel/degrade?mode=slow`
3. 再调 `/sentinel/degrade?mode=ok`
4. 观察熔断窗口

### 第五阶段：做 fallback 理解

1. 调 `/sentinel/degrade?mode=error`
2. 观察业务异常时 fallback 返回
3. 对比 blockHandler 和 fallback 的差异

## blockHandler 和 fallback 的区别

在这个示例里：

- `blockHandler`
  - 处理 Sentinel 规则触发后的限流、熔断、热点拦截
- `fallback`
  - 处理业务方法自身抛出的异常

你可以重点对比这两个场景：

- 规则命中：返回 `type=BLOCKED`
- 业务异常：返回 `type=FALLBACK`

## 测试

```bash
mvn -pl sentinel-demo test
```

当前测试只做了最小上下文校验，目的是保证模块可以正常启动。

## 下一步你可以继续学什么

如果你想继续往下学，这个模块后面可以继续扩展：

- `RestTemplate` 或 OpenFeign 的 Sentinel 整合
- 自定义 `UrlBlockHandler`
- Sentinel 规则持久化到 Nacos
- Gateway 限流
- 集群流控
