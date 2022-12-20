package com.likelion.project.domain.dto.post;

import com.likelion.project.domain.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class PostCreateResponse {

    private Integer postId;
    private String message;

    public static PostCreateResponse of(Post post) {
        return PostCreateResponse.builder()
                .postId(post.getId())
                .message("포스트 등록 완료")
                .build();
    }
}
