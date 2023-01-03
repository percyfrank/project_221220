package com.likelion.project.service;

import com.likelion.project.domain.dto.post.*;
import com.likelion.project.domain.entity.Like;
import com.likelion.project.domain.entity.Post;
import com.likelion.project.domain.entity.User;
import com.likelion.project.exception.ErrorCode;
import com.likelion.project.exception.AppException;
import com.likelion.project.exception.AppException;
import com.likelion.project.repository.LikeRepository;
import com.likelion.project.repository.PostRepository;
import com.likelion.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;


    public PostCreateResponse createPost(PostCreateRequest request, String userName) {

        // Authentication으로 넘어온 userName 확인, 없으면 등록 불가
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_PERMISSION));

        Post post = Post.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .user(user)
                .build();

        Post savedPost = postRepository.save(post);
        return PostCreateResponse.of(savedPost);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> findAllPost(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        return posts.map(PostResponse::of);
    }

    @Transactional(readOnly = true)
    public PostDetailResponse findPost(Integer id) {
        // post 조회
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        return PostDetailResponse.of(post);
    }

    public void update(Integer id, String userName, PostUpdateRequest request) {
        Post post = validatePost(id, userName);
        post.updatePost(request.getTitle(),request.getBody());
    }

    public void delete(Integer id, String userName) {
        Post post = validatePost(id, userName);
        postRepository.deleteById(post.getId());
    }

    private Post validatePost(Integer id, String userName) {
        // Authentication으로 넘어온 userName 확인, 없으면 수정 불가
        userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));

        // 수정할 포스트 존재 확인
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        // 포스트를 수정할 사용자와 포스트를 최초 작성한자의 동일성 검증
        if (!post.getUser().getUserName().equals(userName)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
        return post;
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> findPostsByName(String userName, Pageable pageable) {
        // 로그인 유저 확인
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));

        Page<Post> posts = postRepository.findAllByUser(user, pageable);
        return posts.map(PostResponse::of);
    }

    public void like(Integer postId, String userName) {
        // 로그인 유저 확인
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));

        // 포스트 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        // 좋아요를 눌렀었는지 확인
        likeRepository.findByUserAndPost(user, post)
                .ifPresent(like -> {
                    throw new AppException(ErrorCode.DUPLICATED_LIKE);
                });

        likeRepository.save(new Like(post, user));
    }

    @Transactional(readOnly = true)
    public Long getCountLike(Integer postId) {
        // 포스트 존재 확인
        postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        return likeRepository.countByPost_Id(postId);
    }
}
