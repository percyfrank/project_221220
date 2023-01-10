package com.likelion.project.controller;

import com.likelion.project.domain.dto.comment.CommentDeleteResponse;
import com.likelion.project.domain.dto.comment.CommentRequest;
import com.likelion.project.domain.dto.comment.CommentResponse;
import com.likelion.project.exception.Response;
import com.likelion.project.service.CommentService;
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

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Slf4j
public class CommentApiController {

    private final CommentService commentService;

    // 특정 포스트의 댓글 전체 조회
    @Tag(name = "3. 댓글")
    @Operation(summary = "댓글 리스트 조회", description = "댓글 달린 포스트 id 필요 & 최근 생성 순으로 10개씩 페이징")
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
    @Tag(name = "3. 댓글")
    @Operation(summary = "댓글 작성", description = "권한 필요 & 댓글 내용 및 포스트 id 필요")
    @PostMapping("/{postId}/comments")
    public Response<CommentResponse> addComment(@PathVariable("postId") Integer postId,
            @RequestBody CommentRequest request, @Parameter(hidden = true) Authentication authentication) {
        CommentResponse comment = commentService.create(postId, request, authentication.getName());
        log.info("댓글 작성 성공");
        return Response.success(comment);
    }

    // 댓글 수정
    @Tag(name = "3. 댓글")
    @Operation(summary = "댓글 수정", description = "권한 필요 & 수정 댓글 내용, 댓글 id, 포스트 id 필요")
    @PutMapping("/{postId}/comments/{commentId}")
    public Response<CommentResponse> updateComment(@PathVariable("postId") Integer postId, @PathVariable("commentId") Integer commentId,
            @RequestBody CommentRequest request, @Parameter(hidden = true) Authentication authentication) {
        CommentResponse updatedComment = commentService.update(postId, commentId, request, authentication.getName());
        log.info("댓글 수정 성공");
        return Response.success(updatedComment);

    }

    // 댓글 삭제
    @Tag(name = "3. 댓글")
    @Operation(summary = "댓글 삭제", description = "권한 필요 & 댓글 id, 포스트 id 필요")
    @DeleteMapping("/{postId}/comments/{commentId}")
    public Response<CommentDeleteResponse> deleteComment(
            @PathVariable("postId") Integer postId, @PathVariable("commentId") Integer commentId,
            Authentication authentication) {
        CommentDeleteResponse deletedComment = commentService.delete(postId, commentId, authentication.getName());
        log.info("댓글 삭제 성공");
        return Response.success(deletedComment);
    }
}
