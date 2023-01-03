package com.likelion.project.service;

import com.likelion.project.domain.dto.comment.CommentRequest;
import com.likelion.project.domain.dto.comment.CommentResponse;
import com.likelion.project.domain.entity.Comment;
import com.likelion.project.domain.entity.Post;
import com.likelion.project.domain.entity.User;
import com.likelion.project.exception.AppException;
import com.likelion.project.exception.ErrorCode;
import com.likelion.project.repository.CommentRepository;
import com.likelion.project.repository.PostRepository;
import com.likelion.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public Page<CommentResponse> findAllComments(Integer id, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByPostId(id, pageable);
        return comments.map(CommentResponse::of);
    }

    public CommentResponse createComment(Integer id, CommentRequest request, String userName) {

        // 로그인된 유저 확인
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_PERMISSION));

        // 포스트 존재 확인
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        Comment comment = Comment.builder()
                .comment(request.getComment())
                .user(user)
                .post(post)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return CommentResponse.of(savedComment);
    }

    public CommentResponse update(Integer postId, Integer commentId,
                                  CommentRequest request, String userName) {

        Comment comment = validateComment(postId, commentId, userName);
        CommentResponse updatedComment = comment.updateComment(request.getComment());
        return updatedComment;
    }

    public void delete(Integer postId, Integer commentId, String userName) {
        validateComment(postId, commentId, userName);
        commentRepository.deleteById(commentId);

    }
    private Comment validateComment(Integer postId, Integer commentId, String userName) {
        // 로그인된 유저 확인
        userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));

        // 댓글 존재 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        if(!comment.getUser().getUserName().equals(userName) ||
        !comment.getPost().getId().equals(postId)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
        return comment;
    }
}
