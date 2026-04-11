package com.parallel.learn.sentinel.controller;

import java.util.Map;

import com.parallel.learn.sentinel.service.SentinelDemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sentinel")
public class SentinelDemoController {

    private final SentinelDemoService sentinelDemoService;

    public SentinelDemoController(SentinelDemoService sentinelDemoService) {
        this.sentinelDemoService = sentinelDemoService;
    }

    @GetMapping("/hello")
    public Map<String, Object> hello(@RequestParam(defaultValue = "Sentinel") String name) {
        return sentinelDemoService.hello(name);
    }

    @GetMapping("/hot")
    public Map<String, Object> hot(@RequestParam Long userId,
                                   @RequestParam(defaultValue = "sku-default") String skuId) {
        return sentinelDemoService.hotParam(userId, skuId);
    }

    @GetMapping("/degrade")
    public Map<String, Object> degrade(@RequestParam(defaultValue = "ok") String mode) {
        return sentinelDemoService.degrade(mode);
    }

    @GetMapping("/manual")
    public Map<String, Object> manual(@RequestParam(defaultValue = "manual-user") String name) {
        return sentinelDemoService.manualEntry(name);
    }

    @GetMapping("/rules/guide")
    public Map<String, Object> rulesGuide() {
        return Map.of(
            "flowControl", "Create a flow rule for helloResource or manualEntryResource.",
            "hotSpot", "Create a hotspot rule for hotParamResource and index 0 (userId).",
            "degradeRt", "Create an RT degrade rule for degradeByException, threshold lower than configured delay.",
            "degradeException", "Call /sentinel/degrade?mode=error and set exception ratio/count rule for degradeByException.");
    }
}
