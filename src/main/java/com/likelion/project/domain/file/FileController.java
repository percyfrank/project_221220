package com.likelion.project.domain.file;

import com.likelion.project.exception.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@Slf4j
public class FileUploadController {

    private final S3UploadService s3UploadService;

    @PostMapping("")
    public ResponseEntity<Response<FileUploadResponse>> uploadFile(@RequestParam MultipartFile multipartFile) throws IOException {
        log.info("파일 등록 완료");
        return ResponseEntity.ok().body(Response.success(s3UploadService.saveUploadFile(multipartFile)));
    }
}
