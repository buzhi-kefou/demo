package com.parallel.learn.sentinel;

import static org.assertj.core.api.Assertions.assertThat;

import com.parallel.learn.sentinel.service.SentinelDemoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SentinelDemoApplicationTest {

    @Autowired
    private SentinelDemoService sentinelDemoService;

    @Test
    void shouldLoadContext() {
        assertThat(sentinelDemoService).isNotNull();
    }
}
