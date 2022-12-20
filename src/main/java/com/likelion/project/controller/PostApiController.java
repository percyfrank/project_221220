package com.likelion.project.controller;

import com.likelion.project.domain.dto.post.PostCreateRequest;
import com.likelion.project.domain.dto.post.PostCreateResponse;
import com.likelion.project.domain.dto.post.PostDetailResponse;
import com.likelion.project.domain.dto.post.PostResponse;
import com.likelion.project.exception.Response;
import com.likelion.project.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Slf4j
public class PostApiController {

    private final PostService postService;

    // 포스트 생성
    @PostMapping("")
    public Response<PostCreateResponse> addPost(@RequestBody PostCreateRequest request, Authentication authentication) {
        String userName = authentication.getName();
        log.info("userName : {}",userName);
        log.info("포스트 생성 성공");
        PostCreateResponse post = postService.createPost(request, userName);
        return Response.success(new PostCreateResponse(post.getPostId(), post.getMessage()));
    }

    // 포스트 리스트
    @GetMapping("")
    public ResponseEntity<Page<PostResponse>> postList(
            @PageableDefault(size = 20, sort = {"id"}, direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("포스트 리스트 조회 성공");
        return ResponseEntity.ok().body(postService.findAllPost(pageable));
    }

    // 포스트 상세
    @GetMapping("/{postsId}")
    public ResponseEntity<PostDetailResponse> postDetail(@PathVariable("postsId") Integer id) {
        log.info("포스트 상세 조회 성공");
        return ResponseEntity.ok().body(postService.findPost(id));
    }


}
