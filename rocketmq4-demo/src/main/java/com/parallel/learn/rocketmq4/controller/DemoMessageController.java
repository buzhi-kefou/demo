package com.parallel.learn.rocketmq4.controller;

import com.parallel.learn.rocketmq4.model.BatchMessageRequest;
import com.parallel.learn.rocketmq4.model.DelayMessageRequest;
import com.parallel.learn.rocketmq4.model.DemoMessageRequest;
import com.parallel.learn.rocketmq4.model.DemoSendResult;
import com.parallel.learn.rocketmq4.model.OrderlyMessageRequest;
import com.parallel.learn.rocketmq4.model.TransactionMessageRequest;
import com.parallel.learn.rocketmq4.service.DemoMessageService;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestController
@RequestMapping("/demo")
public class DemoMessageController {

    private final DemoMessageService demoMessageService;

    public DemoMessageController(DemoMessageService demoMessageService) {
        this.demoMessageService = demoMessageService;
    }

    @PostMapping("/normal/send")
    public DemoSendResult sendNormal(@Valid @RequestBody DemoMessageRequest request) {
        return demoMessageService.sendNormal(request);
    }

    @PostMapping("/orderly/send")
    public DemoSendResult sendOrderly(@Valid @RequestBody OrderlyMessageRequest request) {
        return demoMessageService.sendOrderly(request);
    }

    @PostMapping("/delay/send")
    public DemoSendResult sendDelay(@Valid @RequestBody DelayMessageRequest request) {
        return demoMessageService.sendDelay(request);
    }

    @PostMapping("/batch/send")
    public DemoSendResult sendBatch(@Valid @RequestBody BatchMessageRequest request) {
        return demoMessageService.sendBatch(request);
    }

    @PostMapping("/filter/send")
    public DemoSendResult sendFilter(@Valid @RequestBody DemoMessageRequest request) {
        return demoMessageService.sendFiltered(request);
    }

    @PostMapping("/transaction/send")
    public DemoSendResult sendTransaction(@Valid @RequestBody TransactionMessageRequest request) {
        return demoMessageService.sendTransaction(request);
    }

    @PostMapping("/one-way/send")
    public DemoSendResult sendOneWay(@Valid @RequestBody DemoMessageRequest request) {
        return demoMessageService.sendOneWay(request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DemoSendResult> handleValidationException(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest().body(DemoSendResult.failure(exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DemoSendResult> handleException(Exception exception) {
        return ResponseEntity.internalServerError().body(DemoSendResult.failure(exception.getMessage()));
    }
}
