package com.likelion.project.controller;

import com.likelion.project.domain.dto.comment.CommentDeleteResponse;
import com.likelion.project.domain.dto.comment.CommentRequest;
import com.likelion.project.domain.dto.comment.CommentResponse;
import com.likelion.project.exception.Response;
import com.likelion.project.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Slf4j
public class CommentApiController {

    private final CommentService commentService;

    // 특정 포스트의 댓글 전체 조회
    @GetMapping("/{postId}/comments")
    public Response<Page<CommentResponse>> commentList(
            @PathVariable("postId") Integer postId,
            @PageableDefault(size = 10, sort = {"registeredAt"}, direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<CommentResponse> allComments = commentService.findAllComments(postId, pageable);
        log.info("댓글 조회 성공");
        return Response.success(allComments);
    }

    // 댓글 작성
    @PostMapping("/{postId}/comments")
    public Response<CommentResponse> addComment(@PathVariable("postId") Integer postId,
            @RequestBody CommentRequest request, @ApiIgnore Authentication authentication) {
        CommentResponse comment = commentService.create(postId, request, authentication);
        log.info("댓글 작성 성공");
        return Response.success(comment);
    }

    // 댓글 수정
    @PutMapping("/{postId}/comments/{commentId}")
    public Response<CommentResponse> updateComment(@PathVariable("postId") Integer postId, @PathVariable("commentId") Integer commentId,
            @RequestBody CommentRequest request, @ApiIgnore Authentication authentication) {
        CommentResponse updatedComment = commentService.update(postId, commentId, request, authentication);
        log.info("댓글 수정 성공");
        return Response.success(updatedComment);

    }

    // 댓글 삭제
    @DeleteMapping("/{postId}/comments/{commentId}")
    public Response<CommentDeleteResponse> deleteComment(
            @PathVariable("postId") Integer postId, @PathVariable("commentId") Integer commentId,
            Authentication authentication) {
        commentService.delete(postId, commentId, authentication);
        log.info("댓글 삭제 성공");
        return Response.success(new CommentDeleteResponse(commentId, "댓글 삭제 완료"));
    }
}
