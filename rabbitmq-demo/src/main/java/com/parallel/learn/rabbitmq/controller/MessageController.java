package com.parallel.learn.rabbitmq.controller;

import com.parallel.learn.rabbitmq.service.MessageProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 消息发送控制器
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageProducerService messageProducerService;

    /**
     * 发送Direct消息
     */
    @PostMapping("/direct")
    public String sendDirectMessage(@RequestParam String content) {
        messageProducerService.sendDirectMessage(content);
        return "Direct消息发送成功";
    }

    /**
     * 发送Fanout消息
     */
    @PostMapping("/fanout")
    public String sendFanoutMessage(@RequestParam String content) {
        messageProducerService.sendFanoutMessage(content);
        return "Fanout消息发送成功";
    }

    /**
     * 发送Topic消息
     */
    @PostMapping("/topic")
    public String sendTopicMessage(@RequestParam String content) {
        messageProducerService.sendTopicMessage(content);
        return "Topic消息发送成功";
    }

    /**
     * 发送Headers消息
     */
    @PostMapping("/headers")
    public String sendHeadersMessage(@RequestParam String content) {
        messageProducerService.sendHeadersMessage(content);
        return "Headers消息发送成功";
    }

    /**
     * 发送延迟消息
     */
    @PostMapping("/delay")
    public String sendDelayMessage(@RequestParam String content, @RequestParam long delayMillis) {
        messageProducerService.sendDelayMessage(content, delayMillis);
        return "延迟消息发送成功，延迟时间: " + delayMillis + "毫秒";
    }

    /**
     * 发送消息到死信队列
     */
    @PostMapping("/dlq")
    public String sendToDeadLetterQueue(@RequestParam String content) {
        messageProducerService.sendToDeadLetterQueue(content);
        return "消息已发送到死信队列";
    }
}
