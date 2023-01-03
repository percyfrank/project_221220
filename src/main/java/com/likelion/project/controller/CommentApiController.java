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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class CommentApiController {

    private final CommentService commentService;

    // 특정 포스트의 댓글 전체 조회
    @GetMapping("/posts/{postId}/comments")
    public Response<Page<CommentResponse>> commentList(
            @PathVariable("postId") Integer id,
            @PageableDefault(size = 10, sort = {"registeredAt"}, direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<CommentResponse> allComments = commentService.findAllComments(id, pageable);
        log.info("댓글 조회 성공");
        return Response.success(allComments);
    }

    // 댓글 작성
    @PostMapping("/posts/{postsId}/comments")
    public Response<CommentResponse> addComment(
            @PathVariable("postsId") Integer id,
            @RequestBody CommentRequest request, @ApiIgnore Authentication authentication) {
        String userName = authentication.getName();
        CommentResponse comment = commentService.createComment(id, request, userName);
        log.info("댓글 작성 성공");
        return Response.success(comment);
    }

    // 댓글 수정
    @PutMapping("/posts/{postId}/comments/{id}")
    public Response<CommentResponse> updateComment(
            @PathVariable("postId") Integer postId,
            @PathVariable("id") Integer commentId,
            @RequestBody CommentRequest request, @ApiIgnore Authentication authentication) {

        String userName = authentication.getName();
        CommentResponse updatedComment = commentService.update(postId, commentId, request, userName);
        log.info("댓글 수정 성공");
        return Response.success(updatedComment);

    }

    // 댓글 삭제
    @DeleteMapping("/posts/{postsId}/comments/{id}")
    public Response<CommentDeleteResponse> deleteComment(
            @PathVariable("postsId") Integer postId, @PathVariable("id") Integer commentId,
            Authentication authentication) {

        String userName = authentication.getName();
        commentService.delete(postId, commentId, userName);
        log.info("댓글 삭제 성공");
        return Response.success(new CommentDeleteResponse(commentId, "댓글 삭제 완료"));

    }

}
