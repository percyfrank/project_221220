package com.likelion.project.domain.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class UserJoinResponse {

    private Integer userId;
    private String userName;

}
