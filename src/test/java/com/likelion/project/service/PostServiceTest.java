package com.likelion.project.service;

import com.likelion.project.domain.dto.post.*;
import com.likelion.project.domain.dto.user.UserLoginRequest;
import com.likelion.project.domain.entity.Like;
import com.likelion.project.domain.entity.Post;
import com.likelion.project.domain.entity.User;
import com.likelion.project.exception.AppException;
import com.likelion.project.exception.ErrorCode;
import com.likelion.project.repository.LikeRepository;
import com.likelion.project.repository.PostRepository;
import com.likelion.project.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LikeRepository likeRepository;
    @InjectMocks
    private PostService postService;
    private User user;
    private User user2;
    private Post post;
    private Post mockPost;
    private User mockUser;

    @BeforeEach
    void setup() {
        user = User.builder().id(1).userName("user").password("password").build();
        user2 = User.builder().id(2).userName("user2").password("password2").build();
        post = Post.builder().id(1).title("title").body("body").user(user).build();
        mockPost = mock(Post.class);
        mockUser = mock(User.class);
    }

    @Nested
    @DisplayName("조회")
    class PostList {

        @Test
        @DisplayName("포스트 리스트 조회 성공")
        public void findAllPost_success() {

            Post post1 = Post.builder().id(1).title("title").body("body").user(user).build();
            Post post2 = Post.builder().id(3).title("title").body("body").user(user).build();
            PageImpl<Post> postList = new PageImpl<>(List.of(post1, post2));
            PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"registeredAt");

            given(postRepository.findAll(pageable)).willReturn(postList);

            Page<PostResponse> responsePosts = postService.findAllPost(pageable);

            assertThat(responsePosts.getTotalPages()).isEqualTo(1);
            assertThat(responsePosts.getTotalElements()).isEqualTo(2);

            then(postRepository).should(times(1)).findAll(pageable);
        }

        @Test
        @DisplayName("포스트 상세 조회 성공")
        void findPost_success() {

            given(postRepository.findById(post.getId())).willReturn(Optional.of(post));

            PostDetailResponse postDetail = postService.findPost(1);
            assertThat(postDetail.getId()).isEqualTo(1);
            assertThat(postDetail.getTitle()).isEqualTo("title");
            assertThat(postDetail.getBody()).isEqualTo("body");
            assertThat(postDetail.getUserName()).isEqualTo("user");

            then(postRepository).should(times(1)).findById(post.getId());
        }

        @Test
        @DisplayName("포스트 상세 조회 실패 - 게시글 없음")
        public void findPost_fail1() {

            given(postRepository.findById(post.getId())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class, () -> postService.findPost(post.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 포스트가 없습니다.");

            then(postRepository).should(times(1)).findById(post.getId());
        }

        @Test
        @DisplayName("마이피드 조회 성공")
        public void findMyPost_success() throws Exception {

            Post post1 = Post.builder().id(1).title("title").body("body").user(user).build();
            Post post2 = Post.builder().id(3).title("title").body("body").user(user).build();
            PageImpl<Post> postList = new PageImpl<>(List.of(post1, post2));
            PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"registeredAt");

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(user));
            given(postRepository.findAllByUser(user, pageable)).willReturn(postList);

            Page<PostResponse> myPost = postService.findMyPost(post.getUser().getUserName(), pageable);

            assertThat(myPost.getTotalPages()).isEqualTo(1);
            assertThat(myPost.getTotalElements()).isEqualTo(2);

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findAllByUser(user, pageable);
        }

        @Test
        @DisplayName("마이피드 조회 실패 - 해당 유저 없음")
        public void findMyPost_fail1() throws Exception {
            PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"registeredAt");

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> postService.findMyPost(user.getUserName(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.USERNAME_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("Not founded");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
        }
    }

    @Nested
    @DisplayName("포스트 등록")
    class PostCreate {

        UserLoginRequest userLoginRequest = UserLoginRequest.builder().userName("user").password("password").build();
        PostCreateRequest postCreateRequest = PostCreateRequest.builder().title("제목").body("내용").build();

        @Test
        @DisplayName("등록 성공")
        public void createPost_success() {

            given(userRepository.findByUserName(userLoginRequest.getUserName())).willReturn(Optional.of(user));
            given(postRepository.save(any())).willReturn(post);

            PostCreateResponse savedPost = postService.createPost(postCreateRequest, userLoginRequest.getUserName());
            assertThat(savedPost.getPostId()).isEqualTo(post.getId());
            assertThat(savedPost.getMessage()).isEqualTo("포스트 등록 완료");

            then(userRepository).should(times(1)).findByUserName(userLoginRequest.getUserName());
            then(postRepository).should(times(1)).save(any());
        }

        @Test
        @DisplayName("등록 실패 - 유저가 존재하지 않을 때")
        public void createPost_fail() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> postService.createPost(postCreateRequest, user.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("사용자가 권한이 없습니다.");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
        }
    }

    @Nested
    @DisplayName("포스트 수정")
    class PostUpdate {

        PostUpdateRequest request = PostUpdateRequest.builder().title("title").body("body").build();

        @Test
        @DisplayName("수정 성공")
        public void update_success() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(user));
            given(postRepository.findById(post.getId())).willReturn(Optional.of(post));

            PostUpdateResponse postUpdateResponse = postService.update(post.getId(), request, user.getUserName());

            assertThat(postUpdateResponse.getPostId()).isEqualTo(1);
            assertThat(postUpdateResponse.getMessage()).isEqualTo("포스트 수정 완료");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
        }

        @Test
        @DisplayName("수정 실패 : 포스트 존재하지 않음")
        public void update_fail1() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(user));
            given(postRepository.findById(post.getId())).willThrow(new AppException(ErrorCode.POST_NOT_FOUND));

            AppException appException = assertThrows(AppException.class,
                    () -> postService.update(post.getId(), request, user.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 포스트가 없습니다.");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
        }

        @Test
        @DisplayName("수정 실패 : 작성자!=유저")
        public void update_fail2() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(mockUser));
            given(postRepository.findById(post.getId())).willReturn(Optional.of(mockPost));
            given(mockPost.getUser()).willReturn(user2);

            AppException appException = assertThrows(AppException.class,
                    () -> postService.update(post.getId(), request, user.getUserName()));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("사용자가 권한이 없습니다.");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
        }

        @Test
        @DisplayName("수정 실패 : 유저 존재하지 않음")
        public void update_fail3() {

            given(userRepository.findByUserName(user.getUserName())).willThrow(new AppException(ErrorCode.USERNAME_NOT_FOUND));

            AppException appException = assertThrows(AppException.class,
                    () -> postService.update(post.getId(), request, user.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.USERNAME_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("Not founded");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
        }
    }

    @Nested
    @DisplayName("포스트 삭제")
    class PostDelete
    {

        @Test
        @DisplayName("삭제 성공")
        public void delete_success() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(user));
            given(postRepository.findById(post.getId())).willReturn(Optional.of(post));

            PostDeleteResponse postDeleteResponse = assertDoesNotThrow(() -> postService.delete(post.getId(), user.getUserName()));

            assertThat(postDeleteResponse.getPostId()).isEqualTo(1);
            assertThat(postDeleteResponse.getMessage()).isEqualTo("포스트 삭제 완료");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
        }

        @Test
        @DisplayName("삭제 실패 : 유저 존재하지 않음")
        public void delete_fail1() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> postService.delete(post.getId(), user.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.USERNAME_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("Not founded");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
        }

        @Test
        @DisplayName("삭제 실패 : 포스트 존재하지 않음")
        public void delete_fail2() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(user));
            given(postRepository.findById(post.getId())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> postService.delete(post.getId(), user.getUserName()));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 포스트가 없습니다.");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
        }

        @Test
        @DisplayName("삭제 실패 : 작성자와 유저가 같지 않음")
        public void delete_fail3() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(mockUser));
            given(postRepository.findById(post.getId())).willReturn(Optional.of(mockPost));
            given(mockPost.getUser()).willReturn(user2);

            AppException appException = assertThrows(AppException.class,
                    () -> postService.delete(post.getId(), user.getUserName()));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("사용자가 권한이 없습니다.");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
        }
    }

    @Nested
    @DisplayName("좋아요")
    class createLike {

        @Test
        @DisplayName("좋아요 등록 성공")
        public void addLike_success() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(user));
            given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
            given(likeRepository.findByUserAndPost(user, post)).willReturn(Optional.empty());

            String response = assertDoesNotThrow(() -> postService.createLike(post.getId(), user.getUserName()));
            assertThat(response).isEqualTo("좋아요를 눌렀습니다.");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
            then(likeRepository).should(times(1)).findByUserAndPost(user, post);
        }

        @Test
        @DisplayName("좋아요 실패 - 로그인 유저 없음")
        public void addLike_fail1() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> postService.createLike(post.getId(), user.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.USERNAME_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("Not founded");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
        }

        @Test
        @DisplayName("좋아요 실패 - 포스트 없음")
        public void addLike_fail2() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(user));
            given(postRepository.findById(post.getId())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> postService.createLike(post.getId(), user.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 포스트가 없습니다.");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
        }


        @Test
        @DisplayName("좋아요 실패 - 이미 누른 좋아요")
        public void addLike_fail13() {

            Like like = Like.builder().id(1).post(post).user(user).build();

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(user));
            given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
            given(likeRepository.findByUserAndPost(user, post)).willReturn(Optional.of(like));

            AppException appException = assertThrows(AppException.class,
                    () -> postService.createLike(post.getId(), user.getUserName()));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_LIKE);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("좋아요는 한 번만 누를 수 있습니다.");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
            then(likeRepository).should(times(1)).findByUserAndPost(user, post);
        }

        @Test
        @DisplayName("좋아요 갯수 반환 성공")
        public void likeCount_success() {

            given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
            given(likeRepository.countByPost_Id(post.getId())).willReturn(1L);

            Long countResponse = assertDoesNotThrow(() -> postService.getCountLike(post.getId()));
            assertThat(countResponse).isEqualTo(1L);

            then(postRepository).should(times(1)).findById(post.getId());
            then(likeRepository).should(times(1)).countByPost_Id(post.getId());
        }

        @Test
        @DisplayName("좋아요 갯수 반환 실패 - 포스트 없음")
        public void likeCount_fail1() {

            given(postRepository.findById(post.getId())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class, () -> postService.getCountLike(post.getId()));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 포스트가 없습니다.");

            then(postRepository).should(times(1)).findById(post.getId());
        }
    }
}