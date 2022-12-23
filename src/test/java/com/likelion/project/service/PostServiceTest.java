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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private UserService userService;

    private PostCreateRequest postCreateRequest;

    private UserLoginRequest userLoginRequest;


    @Test
    @DisplayName("포스트 상세 조회 성공")
    void findPost_success() throws Exception {
        //given
        User user = User.builder().id(1).userName("user").build();
        Post post = Post.builder().id(1).title("제목").body("내용").user(user).build();
        //when
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        PostDetailResponse postDetail = postService.findPost(1);
        //then
        assertEquals(1,postDetail.getId());
        assertEquals("제목",postDetail.getTitle());
        assertEquals("내용",postDetail.getBody());
        assertEquals("user",postDetail.getUserName());
        verify(postRepository, times(1)).findById(post.getId());
    }

    @Nested
    @DisplayName("포스트 등록")
    class PostCreate {

        @BeforeEach
        void setup() {
            userLoginRequest = UserLoginRequest.builder().userName("user").password("password").build();
            postCreateRequest = PostCreateRequest.builder().title("제목").body("내용").build();
        }

        @AfterEach
        void setdown() {
            postRepository.deleteAll();
        }

        @Test
        @DisplayName("등록 성공")
        public void createPost_success() throws Exception {

            // given
            User user = UserLoginRequest.toEntity(userLoginRequest);
            Post post = PostCreateRequest.toEntity(postCreateRequest);
            Integer fakePostId = 1;
            ReflectionTestUtils.setField(post,"id",fakePostId);
            //when
            when(userRepository.findByUserName(userLoginRequest.getUserName())).thenReturn(Optional.of(user));
            when(postRepository.save(any())).thenReturn(post);
            PostCreateResponse savedPost =
                    postService.createPost(postCreateRequest,userLoginRequest.getUserName());
            //then
            assertEquals(post.getId(),savedPost.getPostId());
            assertEquals("포스트 등록 완료",savedPost.getMessage());
            verify(userRepository, times(1)).findByUserName(userLoginRequest.getUserName());
            verify(postRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("등록 실패 - 유저가 존재하지 않을 때")
        public void createPost_fail() throws Exception {
            //when
            when(userRepository.findByUserName(any())).thenThrow(new AppException(ErrorCode.INVALID_PERMISSION));
            //then
//            assertThatThrownBy(() -> postService.createPost(postCreateRequest, "username"))
//                    .isInstanceOf(AppException.class);
            AppException appException = assertThrows(AppException.class,
                    () -> postService.createPost(postCreateRequest, "username"));
//            assertThat(ErrorCode.INVALID_PERMISSION).isEqualTo(AppException.getErrorCode());
//            assertThat("사용자가 권한이 없습니다.").isEqualTo(AppException.getErrorCode().getMessage());
            assertEquals(ErrorCode.INVALID_PERMISSION,appException.getErrorCode());
            assertEquals("사용자가 권한이 없습니다.",appException.getErrorCode().getMessage());
            verify(userRepository, times(1)).findByUserName(any());
        }
    }

    @Nested
    @DisplayName("포스트 수정")
    class PostUpdate {

        @AfterEach
        void set() {
            postRepository.deleteAll();
        }

        @Test
        @DisplayName("수정 실패 : 포스트 존재하지 않음")
        @WithMockUser
        public void update_fail1() throws Exception {
            //given
            User user = User.builder().id(1).userName("user").build();
            Post post = Post.builder().id(1).title("제목").body("내용").user(user).build();

            //when
            when(userRepository.findByUserName(user.getUserName())).thenReturn(Optional.of(user));
            when(postRepository.findById(post.getId())).thenThrow(new AppException(ErrorCode.POST_NOT_FOUND));

            //then
            AppException appException = assertThrows(AppException.class,
                    () -> postService.update(post.getId(), user.getUserName(), PostUpdateRequest.builder()
                            .title(post.getTitle()).body(post.getBody()).build()));
            assertEquals(ErrorCode.POST_NOT_FOUND, appException.getErrorCode());
            assertEquals("해당 포스트가 없습니다.", appException.getErrorCode().getMessage());
        }

        @Test
        @DisplayName("수정 실패 : 작성자!=유저")
        public void update_fail2() throws Exception {

            //given
            User user1 = User.builder().userName("user1").password("password").build();
            User user2 = User.builder().userName("user2").password("password2").build();
            Post post = Post.builder().id(1).title("title").body("body").user(user1).build();

            //when
            when(userRepository.findByUserName(user2.getUserName()))
                    .thenThrow(new AppException(ErrorCode.INVALID_PERMISSION));
//            when(postRepository.findById(post.getId())).thenReturn(Optional.of(post)); // @MockitoSettings(strictness = Strictness.LENIENT)가 있어야 함
//            when(userRepository.findByUserName(user2.getUserName()))
//                    .thenReturn(Optional.of(User.builder().userName(user2.getUserName()).password(user2.getPassword())
//                            .build()));
            //then
            AppException appException = assertThrows(AppException.class,
                    () -> postService.update(post.getId(), user2.getUserName(),
                            PostUpdateRequest.builder().title(post.getTitle()).body(post.getBody()).build()));

            assertEquals(ErrorCode.INVALID_PERMISSION, appException.getErrorCode());
            assertEquals("사용자가 권한이 없습니다.",appException.getErrorCode().getMessage());

            verify(postRepository, never()).findById(post.getId());
            verify(userRepository, times(1)).findByUserName(user2.getUserName());
        }

        @Test
        @DisplayName("수정 실패 : 유저 존재하지 않음")
        public void update_fail3() throws Exception {

            //given
            User user = User.builder().id(1).userName("user").build();
            Post post = Post.builder().id(1).title("제목").body("내용").user(user).build();
            //when
//            when(postRepository.findById(post.getId())).thenReturn(Optional.of(mock(Post.class))); // stubbing error
            when(userRepository.findByUserName(user.getUserName()))
                    .thenThrow(new AppException(ErrorCode.USERNAME_NOT_FOUND));
//            when(userRepository.findByUserName(user.getUserName())).thenReturn(Optional.empty());
            //then
            AppException appException = assertThrows(AppException.class,
                    () -> postService.update(post.getId(), user.getUserName(), PostUpdateRequest.builder()
                            .title(post.getTitle()).body(post.getBody()).build()));
            assertEquals(ErrorCode.USERNAME_NOT_FOUND, appException.getErrorCode());
            assertEquals("Not founded", appException.getErrorCode().getMessage());
        }
    }

    @Nested
    @DisplayName("포스트 삭제")
    class PostDelete {

        @Test
        @DisplayName("삭제 실패 : 유저 존재하지 않음")
        public void delete_fail1() throws Exception {
            //given

            //when

            //then
        }

        @Test
        @DisplayName("삭제 실패 : 포스트 존재하지 않음")
        public void delete_fail2() throws Exception {
            //given

            //when

            //then
        }

        @Test
        @DisplayName("삭제 실패 : 작성자와 유저가 존재하지 않음")
        public void delete_fail3() throws Exception {
            //given

            //when

            //then
        }

    }

}