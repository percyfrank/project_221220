package com.likelion.project.domain.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class UserDto {

    private Integer id;
    private String userName;
    private String password;

}
