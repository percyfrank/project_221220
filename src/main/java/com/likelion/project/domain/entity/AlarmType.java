package com.likelion.project.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AlarmType {
    NEW_COMMENT_ON_POST("new comment!"), NEW_LIKE_ON_POST("new like");

    private String text;    // 알람 타입에 맞게  new comment!, new like! 입력
}