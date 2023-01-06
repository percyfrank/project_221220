package com.likelion.project.domain.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CommentDeleteResponse {

    private Integer id;
    private String message;

    public static CommentDeleteResponse of(Integer commentId) {
        return new CommentDeleteResponse(commentId, "댓글 삭제 완료");
    }
}
