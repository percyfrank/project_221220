package com.likelion.project.domain.dto.user;

import com.likelion.project.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class UserJoinResponse {

    private Integer userId;
    private String userName;

    public static UserJoinResponse of(User user) {
        return new UserJoinResponse(user.getId(), user.getUserName());
    }
}
