package com.likelion.project.service;

import com.likelion.project.domain.dto.post.PostCreateRequest;
import com.likelion.project.domain.dto.post.PostCreateResponse;
import com.likelion.project.domain.dto.post.PostDetailResponse;
import com.likelion.project.domain.dto.post.PostResponse;
import com.likelion.project.domain.entity.Post;
import com.likelion.project.domain.entity.User;
import com.likelion.project.exception.ErrorCode;
import com.likelion.project.exception.PostException;
import com.likelion.project.exception.UserException;
import com.likelion.project.repository.PostRepository;
import com.likelion.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;


    public PostCreateResponse createPost(PostCreateRequest request, String userName) {

        // Authentication으로 넘어온 userName 확인, 없으면 등록 불가
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new PostException(ErrorCode.USERNAME_NOT_FOUND));

        Post post = Post.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .user(user)
                .build();

        Post savedPost = postRepository.save(post);
        return PostCreateResponse.of(savedPost);
    }

    public Page<PostResponse> findAllPost(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        return posts.map(PostResponse::of);
    }

    public PostDetailResponse findPost(Integer id) {
        // post 조회
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostException(ErrorCode.POST_NOT_FOUND));

        return PostDetailResponse.of(post);

    }


}
