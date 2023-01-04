package com.likelion.project.domain.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd a HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd a HH:mm:ss")
    private LocalDateTime lastModifiedAt;

    public static CommentResponse of(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .comment(comment.getComment())
                .userName(comment.getUser().getUserName())
                .postId(comment.getPost().getId())
                .createdAt(comment.getRegisteredAt())
                .lastModifiedAt(comment.getUpdatedAt())
                .build();
    }
}
