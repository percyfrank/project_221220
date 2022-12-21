package com.likelion.project.controller;

import com.likelion.project.domain.dto.post.*;
import com.likelion.project.exception.Response;
import com.likelion.project.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
        PostCreateResponse post = postService.createPost(request, userName);
        log.info("포스트 생성 성공");
        return Response.success(new PostCreateResponse(post.getPostId(), post.getMessage()));
    }

    // 포스트 리스트
    @GetMapping("")
    public ResponseEntity<Page<PostResponse>> postList(
            @PageableDefault(size = 20, sort = {"id"}, direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<PostResponse> allPost = postService.findAllPost(pageable);
        log.info("포스트 리스트 조회 성공");
        return ResponseEntity.ok().body(allPost);
    }

    // 포스트 상세
    @GetMapping("/{postsId}")
    public ResponseEntity<PostDetailResponse> postDetail(@PathVariable("postsId") Integer id) {
        PostDetailResponse post = postService.findPost(id);
        log.info("포스트 상세 조회 성공");
        return ResponseEntity.ok().body(post);
    }

    // 포스트 수정
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{postsId}")
    public Response<PostUpdateResponse> updatePost(@PathVariable("postsId") Integer id,
                                                   @RequestBody @Valid PostUpdateRequest request,
                                                   Authentication authentication) {
        String userName = authentication.getName();
        postService.update(id, userName, request);
        PostDetailResponse findPost = postService.findPost(id);
//        log.info("포스트 수정 성공");
        return Response.success(new PostUpdateResponse(findPost.getId(), "포스트 수정 완료"));
    }


}
