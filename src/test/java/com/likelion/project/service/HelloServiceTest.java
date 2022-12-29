package com.likelion.project.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class HelloServiceTest {


    HelloService helloService = new HelloService();

    @Test
    @DisplayName("자릿수 합 성공")
    public void sum() throws Exception {
        assertThat(21).isEqualTo(helloService.sumOfDigit(687));

    }
}