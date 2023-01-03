package com.likelion.project.domain.dto.comment;

import com.likelion.project.domain.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Builder
public class CommentResponse {

    private Integer id;
    private String comment;
    private String userName;
    private Integer postId;
    private String createdAt;

    public static CommentResponse of(Comment comment) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd a HH:mm:ss");
        return CommentResponse.builder()
                .id(comment.getId())
                .comment(comment.getComment())
                .userName(comment.getUser().getUserName())
                .postId(comment.getPost().getId())
                .createdAt(simpleDateFormat.format(comment.getRegisteredAt()))
                .build();
    }
}
