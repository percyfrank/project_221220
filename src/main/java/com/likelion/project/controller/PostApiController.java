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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Slf4j
public class PostApiController {

    private final PostService postService;

    // 포스트 생성
    @PostMapping("")
    public Response<PostCreateResponse> addPost(@RequestBody PostCreateRequest request, @ApiIgnore Authentication authentication) {
        PostCreateResponse createdPost = postService.createPost(request, authentication.getName());
        log.info("포스트 생성 성공");
        return Response.success(createdPost);
    }

    // 포스트 리스트
    @GetMapping("")
    public Response<Page<PostResponse>> postList(
            @PageableDefault(size = 20, sort = {"registeredAt"}, direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<PostResponse> allPost = postService.findAllPost(pageable);
        log.info("포스트 리스트 조회 성공");
//        return ResponseEntity.ok().body(allPost);
        return Response.success(allPost);
    }

    // 포스트 상세
    @GetMapping("/{postsId}")
    public Response<PostDetailResponse> postDetail(@PathVariable("postsId") Integer id) {
        PostDetailResponse postDetail = postService.findPost(id);
        log.info("포스트 상세 조회 성공");
        return Response.success(postDetail);
    }

    // 포스트 수정
    @PutMapping("/{postsId}")
    public Response<PostUpdateResponse> updatePost(@PathVariable("postsId") Integer id,
                                                   @RequestBody @Valid PostUpdateRequest request,
                                                   Authentication authentication) {
        PostUpdateResponse updatedPost = postService.update(id, request, authentication.getName());
        log.info("포스트 수정 성공");
        return Response.success(updatedPost);
    }

    // 포스트 삭제
    @DeleteMapping("/{postsId}")
    public Response<PostDeleteResponse> deletePost(@PathVariable("postsId") Integer id,
                                                   Authentication authentication) {
        PostDeleteResponse deletedPost = postService.delete(id, authentication.getName());
        log.info("포스트 삭제 성공");
        return Response.success(deletedPost);
    }

    // 마이피드 조회
    @GetMapping("/my")
    public Response<Page<PostResponse>> myPostList(
            @PageableDefault(size = 20, sort = {"registeredAt"}, direction = Sort.Direction.DESC)
            Pageable pageable, Authentication authentication) {
        Page<PostResponse> posts = postService.findMyPost(authentication.getName(), pageable);
        log.info("마이피드 조회 성공");
        return Response.success(posts);
    }

    // 좋아요 누르기
    @PostMapping("/{postId}/likes")
    public Response<String> addLike(@PathVariable("postId") Integer postId,
                                             Authentication authentication) {
        postService.createLike(postId, authentication.getName());
        log.info("좋아요 누르기 성공");
        return Response.success("좋아요를 눌렀습니다.");
    }

    // 좋아요 개수
    @GetMapping("/{postId}/likes")
    public Response<Long> countLike(@PathVariable("postId") Integer postId) {
        Long counts = postService.getCountLike(postId);
        log.info("좋아요 개수 반환 성공");
        return Response.success(counts);
    }
}
