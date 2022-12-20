package com.likelion.project.domain.dto.post;

import com.likelion.project.domain.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Builder
public class PostResponse {

    private Integer id;
    private String title;
    private String body;
    private String userName;
    private LocalDateTime createdAt;

    public static PostResponse of(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(post.getBody())
                .userName(post.getUser().getUserName())
                .createdAt(post.getRegisteredAt())
                .build();
    }


}
