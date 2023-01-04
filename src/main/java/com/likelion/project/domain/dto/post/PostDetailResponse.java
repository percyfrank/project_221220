package com.likelion.project.domain.dto.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.likelion.project.domain.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Builder
public class PostDetailResponse {

    private Integer id;
    private String title;
    private String body;
    private String userName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd a HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd a HH:mm:ss")
    private LocalDateTime lastModifiedAt;

    public static PostDetailResponse of(Post post) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(post.getBody())
                .userName(post.getUser().getUserName())
                .createdAt(post.getRegisteredAt())
                .lastModifiedAt(post.getUpdatedAt())
                .build();
    }
}
