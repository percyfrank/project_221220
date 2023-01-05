package com.likelion.project.service;

import com.likelion.project.domain.dto.comment.CommentRequest;
import com.likelion.project.domain.dto.comment.CommentResponse;
import com.likelion.project.domain.entity.*;
import com.likelion.project.exception.AppException;
import com.likelion.project.exception.ErrorCode;
import com.likelion.project.repository.AlarmRepository;
import com.likelion.project.repository.CommentRepository;
import com.likelion.project.repository.PostRepository;
import com.likelion.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final AlarmRepository alarmRepository;

    @Transactional(readOnly = true)
    public Page<CommentResponse> findAllComments(Integer postId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByPostId(postId, pageable);
        return comments.map(CommentResponse::of);
    }

    public CommentResponse create(Integer postId, CommentRequest request, Authentication authentication) {

        // 유저 이름 추출
        String userName = authentication.getName();

        // 로그인된 유저 확인
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_PERMISSION));

        // 포스트 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        // 댓글 DTO로 변환
        Comment comment = Comment.builder().comment(request.getComment()).user(user).post(post).build();

        // 댓글 저장
        Comment savedComment = commentRepository.save(comment);

        // 자기 자신의 게시물인지 확인
        if(!post.getUser().getUserName().equals(userName)) {
            // 댓글 달리면 알림도 저장
            alarmRepository.save(Alarm.builder()
                    .user(post.getUser())
                    .alarmType(AlarmType.NEW_COMMENT_ON_POST)
                    .fromUserId(user.getId())
                    .targetId(post.getId())
                    .text("new comment!")
                    .build());
        }

        return CommentResponse.of(savedComment);
    }

    public CommentResponse update(Integer postId, Integer commentId,
                                  CommentRequest request, Authentication authentication) {

        String userName = authentication.getName();
        // 검증
        Comment comment = validateComment(postId, commentId, userName);
        CommentResponse updatedComment = comment.updateComment(request.getComment());
        return updatedComment;
    }

    public void delete(Integer postId, Integer commentId, Authentication authentication) {
        String userName = authentication.getName();
        // 검증
        validateComment(postId, commentId, userName);
        commentRepository.deleteById(commentId);
    }

    private Comment validateComment(Integer postId, Integer commentId, String userName) {
        // 로그인된 유저 확인
        userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));

        // 포스트 존재 확인
        postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        // 댓글 존재 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        // 최초 댓글 작성 유저와 최초 댓글을 단 포스트를 요청과 일치하는지 확인
        if(!comment.getUser().getUserName().equals(userName) ||
        !comment.getPost().getId().equals(postId)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
        return comment;
    }
}
