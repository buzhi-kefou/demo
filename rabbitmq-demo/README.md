# RabbitMQ学习演示模块

本模块是一个基于Spring Boot的RabbitMQ学习演示项目，展示了RabbitMQ的各种消息类型和功能特性。

## 功能特性

本模块演示了RabbitMQ的以下功能：

1. **Direct Exchange（直连交换机）**：消息根据路由键精确匹配发送到队列
2. **Fanout Exchange（扇形交换机）**：消息广播到所有绑定的队列
3. **Topic Exchange（主题交换机）**：消息根据通配符匹配路由键发送到队列
4. **Headers Exchange（头交换机）**：消息根据消息头属性匹配发送到队列
5. **延迟消息**：消息在指定时间后才被消费者接收
6. **死信队列**：处理无法被正常消费的消息
7. **消息确认机制**：手动确认消息，确保消息可靠投递

## 项目结构

```
rabbitmq-demo/
├── src/main/java/com/parallel/learn/rabbitmq/
│   ├── config/               # 配置类
│   │   └── RabbitMQConfig.java  # RabbitMQ配置，定义交换机、队列和绑定关系
│   ├── controller/           # 控制器
│   │   └── MessageController.java  # 消息发送REST接口
│   ├── model/                # 实体类
│   │   └── Message.java      # 消息实体
│   ├── service/              # 服务类
│   │   ├── MessageConsumerService.java  # 消息消费者
│   │   └── MessageProducerService.java  # 消息生产者
│   └── RabbitMQDemoApplication.java  # 应用启动类
└── src/main/resources/
    └── application.yml       # 应用配置文件
```

## 快速开始

### 前置条件

1. 安装JDK 17或更高版本
2. 安装Maven
3. 安装并启动RabbitMQ服务

### 运行步骤

1. 克隆或下载本项目
2. 进入rabbitmq-demo目录
3. 运行以下命令启动应用：
   ```
   mvn spring-boot:run
   ```
4. 应用启动后，可以通过以下REST接口发送消息：

#### 发送Direct消息
```
POST http://localhost:8084/api/message/direct?content=Hello Direct
```

#### 发送Fanout消息
```
POST http://localhost:8084/api/message/fanout?content=Hello Fanout
```

#### 发送Topic消息
```
POST http://localhost:8084/api/message/topic?content=Hello Topic
```

#### 发送Headers消息
```
POST http://localhost:8084/api/message/headers?content=Hello Headers
```

#### 发送延迟消息
```
POST http://localhost:8084/api/message/delay?content=Hello Delay&delayMillis=5000
```

#### 发送消息到死信队列
```
POST http://localhost:8084/api/message/dlq?content=Hello DLQ
```

## 配置说明

在`application.yml`文件中，可以配置以下参数：

- RabbitMQ连接信息（host、port、username、password等）
- 消息确认机制配置
- 交换机、队列和路由键名称
- 消费者并发配置
- 延迟消息配置

## 学习建议

1. 从Direct Exchange开始，理解基本的消息发送和接收流程
2. 尝试不同类型的Exchange，了解它们之间的区别
3. 实验延迟消息和死信队列，理解高级消息特性
4. 修改配置参数，观察消息处理行为的变化
5. 使用RabbitMQ管理界面查看队列和消息状态

## 参考资料

- [RabbitMQ官方文档](https://www.rabbitmq.com/documentation.html)
- [Spring AMQP文档](https://docs.spring.io/spring-amqp/reference/)
- [RabbitMQ教程](https://www.rabbitmq.com/getstarted.html)
