package com.likelion.project.controller;

import com.likelion.project.service.HelloService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping ("/api/v1/hello")
public class HelloController {

    private final HelloService helloService;
    @GetMapping("")
    public String hello() {
        return "권오석";
    }

    @GetMapping("{num}")
    public Integer sumofDigit(@PathVariable("num") Integer num) {
        return helloService.sumOfDigit(num);
    }
}
