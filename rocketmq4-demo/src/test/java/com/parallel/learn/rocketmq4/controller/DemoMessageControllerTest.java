package com.parallel.learn.rocketmq4.controller;

import com.parallel.learn.rocketmq4.model.DemoSendResult;
import com.parallel.learn.rocketmq4.service.DemoMessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DemoMessageController.class)
class DemoMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DemoMessageService demoMessageService;

    @Test
    void shouldSendNormalMessage() throws Exception {
        when(demoMessageService.sendNormal(any()))
                .thenReturn(DemoSendResult.success("msg-1", "SEND_OK", "Normal message sent"));

        mockMvc.perform(post("/demo/normal/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"hello rocketmq\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageId").value("msg-1"));
    }

    @Test
    void shouldRejectInvalidBatchRequest() throws Exception {
        mockMvc.perform(post("/demo/batch/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"messages\":[]}"))
                .andExpect(status().isBadRequest());
    }
}
