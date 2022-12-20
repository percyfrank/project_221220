package com.likelion.project.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserException extends RuntimeException {

    private ErrorCode errorCode;

    public UserException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}
