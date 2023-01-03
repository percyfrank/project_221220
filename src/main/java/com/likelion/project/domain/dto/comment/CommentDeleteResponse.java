package com.likelion.project.domain.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CommentDeleteResponse {

    private Integer id;
    private String message;
}
