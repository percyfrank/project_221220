package com.likelion.project.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Stream;

@Service
public class HelloService {

    public Integer sum(String num) {
        int sum = Stream.of(num.split("")).mapToInt((a) -> Integer.valueOf(a)).sum();
        return sum;
    }

    public Integer sumOfDigit(int num) {
        int sum = 0;
        while(num > 0) {
            sum += num % 10;
            num /= 10;
        }
        return sum;
    }

    public static void main(String[] args) {
        HelloService h = new HelloService();
        System.out.println(h.sum("687"));
    }
}
