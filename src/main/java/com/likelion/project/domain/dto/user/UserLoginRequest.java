package com.likelion.project.domain.dto.user;

import com.likelion.project.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder

public class UserLoginRequest {

    private String userName;
    private String password;

    public static User toEntity(UserLoginRequest userLoginRequest) {
        return User.builder()
                .userName(userLoginRequest.getUserName())
                .password(userLoginRequest.getPassword())
                .build();
    }
}
