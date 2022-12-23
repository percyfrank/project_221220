package com.likelion.project.domain.dto.post;

import com.likelion.project.domain.dto.user.UserLoginRequest;
import com.likelion.project.domain.entity.Post;
import com.likelion.project.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class PostCreateRequest {

    private String title;
    private String body;

    public static Post toEntity(PostCreateRequest postCreateRequest) {
        return Post.builder()
                .title(postCreateRequest.getTitle())
                .body(postCreateRequest.getBody())
                .build();
    }
}
