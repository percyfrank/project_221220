package com.likelion.project.domain.file;

import com.likelion.project.domain.dto.post.PostCreateResponse;
import com.likelion.project.domain.entity.Post;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResponse {

    private String uploadFileName;
    private String message;

    public static FileUploadResponse of(UploadFile uploadFile) {
        return FileUploadResponse.builder()
                .uploadFileName(uploadFile.getUploadFileName())
                .message("파일 첨부 완료")
                .build();
    }
}
