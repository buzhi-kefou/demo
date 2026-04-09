# RocketMQ 4 Spring Boot Demo

这个模块演示了 RocketMQ 4 在 Spring Boot 中的常见消息类型，包括普通消息、顺序消息、延时消息、批量消息、Tag 过滤消息、事务消息和单向消息。

## 环境要求

- JDK 17
- Maven 3.8+
- 可访问的 RocketMQ 4 NameServer 和 Broker

## 配置

默认配置位于 `src/main/resources/application.yml`：

```yaml
rocketmq:
  name-server: 127.0.0.1:9876
  producer:
    group: demo-tx-producer-group

demo:
  rocketmq:
    consumers-enabled: false
    normal-topic: demo-normal-topic
    orderly-topic: demo-orderly-topic
    delay-topic: demo-delay-topic
    batch-topic: demo-batch-topic
    filter-topic: demo-filter-topic
    transaction-topic: demo-transaction-topic
    one-way-topic: demo-one-way-topic
    default-tag: demo
    filter-tag: blue
```

- `rocketmq.name-server` 指向你的 RocketMQ 4 NameServer。
- `demo.rocketmq.consumers-enabled=true` 时会注册所有示例消费者，便于观察消费日志。
- 事务消息示例使用 `demo-tx-producer-group` 和 `demoTransactionExecutor`。

## 启动

```bash
mvn -pl rocketmq4-demo spring-boot:run
```

## HTTP 示例

```bash
curl -X POST http://localhost:8080/demo/normal/send -H "Content-Type: application/json" -d "{\"body\":\"hello normal\"}"
curl -X POST http://localhost:8080/demo/orderly/send -H "Content-Type: application/json" -d "{\"body\":\"hello orderly\",\"shardingKey\":\"order-1\"}"
curl -X POST http://localhost:8080/demo/delay/send -H "Content-Type: application/json" -d "{\"body\":\"hello delay\",\"delayLevel\":4}"
curl -X POST http://localhost:8080/demo/batch/send -H "Content-Type: application/json" -d "{\"messages\":[{\"body\":\"batch-1\"},{\"body\":\"batch-2\"}]}"
curl -X POST http://localhost:8080/demo/filter/send -H "Content-Type: application/json" -d "{\"body\":\"only blue consumer should receive\",\"tag\":\"blue\"}"
curl -X POST http://localhost:8080/demo/transaction/send -H "Content-Type: application/json" -d "{\"body\":\"tx body\",\"businessId\":\"commit-order-1001\"}"
curl -X POST http://localhost:8080/demo/one-way/send -H "Content-Type: application/json" -d "{\"body\":\"fire and forget\"}"
```

`businessId` 包含 `commit` / `success` 会提交事务，包含 `rollback` / `fail` 会回滚，否则返回 `UNKNOW` 以演示回查流程。

## 学习提示

- 顺序消息请多次使用同一个 `shardingKey`，观察消费顺序。
- 延时消息的 `delayLevel` 使用 RocketMQ 4 预定义级别。
- 过滤消息默认只消费匹配 `blue` tag 的消息。
- 单向消息不会等待 Broker 返回发送结果，更适合日志采集等场景。
