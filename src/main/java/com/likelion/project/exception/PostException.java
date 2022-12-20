package com.likelion.project.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PostException extends RuntimeException{

    private ErrorCode errorCode;

    public PostException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
