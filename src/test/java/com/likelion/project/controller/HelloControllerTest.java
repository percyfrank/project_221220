package com.likelion.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.project.service.HelloService;
import com.likelion.project.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HelloController.class)
class HelloControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    HelloService helloService;

    @Test
    @DisplayName("자릿수 합")
    @WithMockUser
    public void sum() throws Exception {
        given(helloService.sumOfDigit(687)).willReturn(21);

        mockMvc.perform(get("/api/v1/hello/687"))
                .andExpect(status().isOk())
                .andExpect(content().string("21"))
                .andDo(print());
    }



}