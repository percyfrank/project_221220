package com.likelion.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @RequestMapping ("/api/v1/hello")
    public String hello() {
        return "darkchocolate";
    }
}
