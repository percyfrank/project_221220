package com.likelion.project.domain.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class PostDeleteResponse {

    private Integer postId;
    private String message;

    public static PostDeleteResponse of(Integer postId) {
        return PostDeleteResponse.builder()
                .postId(postId)
                .message("포스트 삭제 완료")
                .build();
    }
}
