package com.likelion.project.service;

import org.springframework.stereotype.Service;

@Service
public class HelloService {

    public Integer sumOfDigit(int num) {
        int sum = 0;
        while(num > 0) {
            sum += num % 10;
            num /= 10;
        }

        return sum;
    }
}
