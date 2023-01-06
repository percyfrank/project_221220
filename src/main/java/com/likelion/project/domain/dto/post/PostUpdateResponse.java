package com.likelion.project.domain.dto.post;

import com.likelion.project.domain.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class PostUpdateResponse {

    private Integer postId;
    private String message;

    public static PostUpdateResponse of(Integer postId) {
        return PostUpdateResponse.builder()
                .postId(postId)
                .message("포스트 수정 완료")
                .build();
    }
}
