package com.parallel.learn.flowable.demo;

import com.parallel.learn.flowable.client.starter.RemoteFlowableProcessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.cloud.nacos.discovery.enabled=false",
    "spring.cloud.service-registry.auto-registration.enabled=false"
})
class FlowableProcessDemoApplicationTest {

    @Autowired
    private RemoteFlowableProcessService remoteFlowableProcessService;

    @Test
    void shouldLoadRemoteFlowableClientBean() {
        assertThat(remoteFlowableProcessService).isNotNull();
    }
}
