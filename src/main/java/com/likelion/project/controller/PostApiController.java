package com.likelion.project.controller;

import com.likelion.project.domain.dto.post.*;
import com.likelion.project.exception.Response;
import com.likelion.project.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    @Tag(name = "2. 게시글")
    @Operation(summary = "게시글 작성", description = "권한 필요 & 요청 데이터 title, body")
    @PostMapping("")
    public Response<PostCreateResponse> addPost(@RequestBody PostCreateRequest request, @Parameter(hidden = true) Authentication authentication) {
        PostCreateResponse createdPost = postService.createPost(request, authentication.getName());
        log.info("포스트 생성 성공");
        return Response.success(createdPost);
    }

    // 포스트 리스트
    @Tag(name = "2. 게시글")
    @Operation(summary = "게시글 리스트 조회", description = "최근 생성 순으로 20개 페이징")
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
    @Tag(name = "2. 게시글")
    @Operation(summary = "게시글 상세 조회", description = "아이디,제목,내용,작성유저,생성날짜,수정날짜 반환")
    @GetMapping("/{postsId}")
    public Response<PostDetailResponse> postDetail(@PathVariable("postsId") Integer id) {
        PostDetailResponse postDetail = postService.findPost(id);
        log.info("포스트 상세 조회 성공");
        return Response.success(postDetail);
    }

    // 포스트 수정
    @Tag(name = "2. 게시글")
    @Operation(summary = "게시글 수정", description = "권한 필요 & 요청 데이터 title, body")
    @PutMapping("/{postsId}")
    public Response<PostUpdateResponse> updatePost(@PathVariable("postsId") Integer id,
                                                   @RequestBody @Valid PostUpdateRequest request,
                                                   @Parameter(hidden = true) Authentication authentication) {
        PostUpdateResponse updatedPost = postService.update(id, request, authentication.getName());
        log.info("포스트 수정 성공");
        return Response.success(updatedPost);
    }

    // 포스트 삭제
    @Tag(name = "2. 게시글")
    @Operation(summary = "게시글 삭제", description = "권한 필요 & 삭제하고자 하는 게시글 id 필요")
    @DeleteMapping("/{postsId}")
    public Response<PostDeleteResponse> deletePost(@PathVariable("postsId") Integer id,
                                                   @Parameter(hidden = true) Authentication authentication) {
        PostDeleteResponse deletedPost = postService.delete(id, authentication.getName());
        log.info("포스트 삭제 성공");
        return Response.success(deletedPost);
    }

    // 마이피드 조회
    @Tag(name = "5. 마이피드")
    @Operation(summary = "마이피드 리스트 조회", description = "권한 필요 & 내가 작성한 게시글 최근 생성 순으로 20개씩 페이징")
    @GetMapping("/my")
    public Response<Page<PostResponse>> myPostList(
            @PageableDefault(size = 20, sort = {"registeredAt"}, direction = Sort.Direction.DESC)
            Pageable pageable, @Parameter(hidden = true) Authentication authentication) {
        Page<PostResponse> posts = postService.findMyPost(authentication.getName(), pageable);
        log.info("마이피드 조회 성공");
        return Response.success(posts);
    }

    // 좋아요 누르기
    @Tag(name = "4. 좋아요")
    @Operation(summary = "좋아요 누르기", description = "권한 필요 & 좋아요 누를 게시글 id 필요")
    @PostMapping("/{postId}/likes")
    public Response<String> addLike(@PathVariable("postId") Integer postId,
                                    @Parameter(hidden = true) Authentication authentication) {
        String response = postService.createLike(postId, authentication.getName());
        log.info("좋아요 누르기 성공");
        return Response.success(response);
    }

    // 좋아요 개수
    @Tag(name = "4. 좋아요")
    @Operation(summary = "좋아요 갯수 반환", description = "갯수 반환할 게시글 id 필요")
    @GetMapping("/{postId}/likes")
    public Response<Long> countLike(@PathVariable("postId") Integer postId) {
        Long counts = postService.getCountLike(postId);
        log.info("좋아요 개수 반환 성공");
        return Response.success(counts);
    }
}
