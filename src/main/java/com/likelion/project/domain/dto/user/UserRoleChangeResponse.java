package com.likelion.project.domain.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleChangeResponse {
    private Integer userId;
    private String message;
}
