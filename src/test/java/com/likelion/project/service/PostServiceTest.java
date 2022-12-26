package com.likelion.project.service;

import com.likelion.project.domain.dto.post.*;
import com.likelion.project.domain.dto.user.UserLoginRequest;
import com.likelion.project.domain.entity.Post;
import com.likelion.project.domain.entity.User;
import com.likelion.project.exception.ErrorCode;
import com.likelion.project.exception.AppException;
import com.likelion.project.repository.PostRepository;
import com.likelion.project.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
//import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT) // stubbing error
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
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

    @Test
    @DisplayName("포스트 상세 조회 성공")
    void findPost_success() throws Exception {
        given(postRepository.findById(post.getId())).willReturn(Optional.of(post));

        PostDetailResponse postDetail = postService.findPost(1);
        assertThat(postDetail.getId()).isEqualTo(1);
        assertThat(postDetail.getTitle()).isEqualTo("title");
        assertThat(postDetail.getBody()).isEqualTo("body");
        assertThat(postDetail.getUserName()).isEqualTo("user");

        verify(postRepository, times(1)).findById(post.getId());
    }

    @Nested
    @DisplayName("포스트 등록")
    class PostCreate {

        @Test
        @DisplayName("등록 성공")
        public void createPost_success() throws Exception {
            UserLoginRequest userLoginRequest = UserLoginRequest.builder().userName("user").password("password").build();
            PostCreateRequest postCreateRequest = PostCreateRequest.builder().title("제목").body("내용").build();
            User user = UserLoginRequest.toEntity(userLoginRequest);
            Post post = PostCreateRequest.toEntity(postCreateRequest);
            Integer fakePostId = 1;
            ReflectionTestUtils.setField(post,"id",fakePostId);

            given(userRepository.findByUserName(userLoginRequest.getUserName())).willReturn(Optional.of(user));
            given(postRepository.save(any())).willReturn(post);

            PostCreateResponse savedPost = postService.createPost(postCreateRequest,userLoginRequest.getUserName());
            assertThat(savedPost.getPostId()).isEqualTo(post.getId());
            assertThat(savedPost.getMessage()).isEqualTo("포스트 등록 완료");

            verify(userRepository, times(1)).findByUserName(userLoginRequest.getUserName());
            verify(postRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("등록 실패 - 유저가 존재하지 않을 때")
        public void createPost_fail() throws Exception {
//            UserLoginRequest userLoginRequest = UserLoginRequest.builder().userName("user").password("password").build();
            PostCreateRequest postCreateRequest = PostCreateRequest.builder().title("제목").body("내용").build();

            given(userRepository.findByUserName(any())).willThrow(new AppException(ErrorCode.INVALID_PERMISSION));

            AppException appException = assertThrows(AppException.class,
                    () -> postService.createPost(postCreateRequest, "username"));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("사용자가 권한이 없습니다.");

            verify(userRepository, times(1)).findByUserName(any());
        }
    }

    @Nested
    @DisplayName("포스트 수정")
    class PostUpdate {

        @Test
        @DisplayName("수정 실패 : 포스트 존재하지 않음")
        @WithMockUser
        public void update_fail1() throws Exception {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(user));
            given(postRepository.findById(post.getId())).willThrow(new AppException(ErrorCode.POST_NOT_FOUND));

            AppException appException = assertThrows(AppException.class,
                    () -> postService.update(post.getId(), user.getUserName(), PostUpdateRequest.builder()
                            .title(post.getTitle()).body(post.getBody()).build()));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 포스트가 없습니다.");
        }

        @Test
        @DisplayName("수정 실패 : 작성자!=유저")
        public void update_fail2() throws Exception {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(mockUser));
            given(postRepository.findById(post.getId())).willReturn(Optional.of(mockPost));
            given(mockPost.getUser()).willReturn(user2);

//            given(postRepository.findById(post.getId())).willReturn(Optional.of(post)); // @MockitoSettings(strictness = Strictness.LENIENT)가 있어야 함

            AppException appException = assertThrows(AppException.class,
                    () -> postService.update(post.getId(), user.getUserName(),
                            PostUpdateRequest.builder().title(post.getTitle()).body(post.getBody()).build()));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("사용자가 권한이 없습니다.");

            verify(userRepository, times(1)).findByUserName(user.getUserName());
        }

        @Test
        @DisplayName("수정 실패 : 유저 존재하지 않음")
        public void update_fail3() throws Exception {

            given(userRepository.findByUserName(user.getUserName())).willThrow(new AppException(ErrorCode.USERNAME_NOT_FOUND));

            AppException appException = assertThrows(AppException.class,
                    () -> postService.update(post.getId(), user.getUserName(),
                            PostUpdateRequest.builder().title(post.getTitle()).body(post.getBody()).build()));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.USERNAME_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("Not founded");
        }
    }

    @Nested
    @DisplayName("포스트 삭제")
    class PostDelete {

        @Test
        @DisplayName("삭제 실패 : 유저 존재하지 않음")
        public void delete_fail1() throws Exception {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.empty());
//            given(userRepository.findByUserName(user.getUserName()))
//                    .willThrow(new AppException(ErrorCode.USERNAME_NOT_FOUND));

            AppException appException = assertThrows(AppException.class,
                    () -> postService.delete(post.getId(), user.getUserName()));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.USERNAME_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("Not founded");
        }

        @Test
        @DisplayName("삭제 실패 : 포스트 존재하지 않음")
        public void delete_fail2() throws Exception {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(mockUser));
//            given(postRepository.findById(post.getId())).willThrow(new AppException(ErrorCode.POST_NOT_FOUND));
            given(postRepository.findById(post.getId())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> postService.delete(post.getId(), user.getUserName()));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 포스트가 없습니다.");
        }

        @Test
        @DisplayName("삭제 실패 : 작성자와 유저가 같지 않음")
        public void delete_fail3() throws Exception {

            given(userRepository.findByUserName(user.getUserName()))
                    .willReturn(Optional.of(mockUser));
            given(postRepository.findById(post.getId())).willReturn(Optional.of(mockPost));
            given(mockPost.getUser()).willReturn(user2);

            AppException appException = assertThrows(AppException.class,
                    () -> postService.delete(post.getId(), user.getUserName()));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("사용자가 권한이 없습니다.");

            verify(userRepository, times(1)).findByUserName(user.getUserName());
        }
    }
}