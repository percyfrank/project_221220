package com.likelion.project.service;

import com.likelion.project.domain.dto.post.*;
import com.likelion.project.domain.entity.Post;
import com.likelion.project.domain.entity.User;
import com.likelion.project.exception.ErrorCode;
import com.likelion.project.exception.AppException;
import com.likelion.project.exception.AppException;
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
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;


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
    @Transactional
    public Page<PostResponse> findAllPost(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        return posts.map(PostResponse::of);
    }
    @Transactional
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
}
