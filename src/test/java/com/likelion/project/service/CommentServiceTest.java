package com.likelion.project.service;

import com.likelion.project.domain.dto.comment.CommentDeleteResponse;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @InjectMocks
    private CommentService commentService;
    private User user;
    private Post post;
    private Comment comment;
    private Comment comment2;
    private Post mockPost;
    private User mockUser;
    private Comment mockComment;

    @BeforeEach
    void setup() {
        user = User.builder().id(1).userName("user").password("password").build();
        post = Post.builder().id(1).title("title").body("body").user(user).build();
        comment = Comment.builder().id(1).comment("comment").user(user).post(post).build();
        comment2 = Comment.builder().id(2).comment("comment").user(user).post(post).build();
        mockPost = mock(Post.class);
        mockUser = mock(User.class);
        mockComment = mock(Comment.class);
    }

    @Nested
    @DisplayName("??????")
    class ReadComment {

        @Test
        @DisplayName("?????? ????????? ?????? ??????")
        public void commentList_success() {

            Page<Comment> commentsList = new PageImpl<>(List.of(comment, comment2));
            PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"registeredAt");

            given(commentRepository.findByPostId(post.getId(), pageable)).willReturn(commentsList);

            Page<CommentResponse> responsePage = commentService.findAllComments(post.getId(), pageable);

            assertThat(responsePage.getTotalPages()).isEqualTo(1);
            assertThat(responsePage.getTotalElements()).isEqualTo(2);

            then(commentRepository).should(times(1)).findByPostId(post.getId(), pageable);
        }
    }

    @Nested
    @DisplayName("??????")
    class CommentCreate {

        CommentRequest request = new CommentRequest("comment");

        @Test
        @DisplayName("?????? ?????? ??????")
        public void createComment_success() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(user));
            given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
            given(commentRepository.save(any(Comment.class))).willReturn(comment);

            CommentResponse response = commentService.create(post.getId(), request, user.getUserName());
            assertThat(response.getId()).isEqualTo(comment.getId());
            assertThat(response.getComment()).isEqualTo(request.getComment());
            assertThat(response.getUserName()).isEqualTo(user.getUserName());
            assertThat(response.getPostId()).isEqualTo(post.getId());

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
        }

        @Test
        @DisplayName("?????? ?????? ?????? - ?????? ??????")
        public void createComment_fail1() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class, () -> commentService.create(post.getId(), request, user.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("???????????? ????????? ????????????.");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
        }

        @Test
        @DisplayName("?????? ?????? ?????? - ????????? ??????")
        public void createComment_fail2() {
            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(user));
            given(postRepository.findById(post.getId())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class, () -> commentService.create(post.getId(), request, user.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ???????????? ????????????.");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());

        }

//        @Test
//        @DisplayName("?????? ?????? ??????")
//        public void saveAlarm_success() {
//
//            Alarm alarm = Alarm.builder().user(post.getUser()).alarmType(AlarmType.NEW_COMMENT_ON_POST).fromUserId(user.getId())
//                    .targetId(post.getId()).build();
//
//            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(user));
//            given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
//            given(commentRepository.save(any(Comment.class))).willReturn(comment);
//            given(alarmRepository.save(any(Alarm.class))).willReturn(alarm);
//
//            commentService.create(post.getId(), request, user.getUserName());
//
////            assertThat(response.getId()).isEqualTo(comment.getId());
////            assertThat(response.getComment()).isEqualTo(request.getComment());
////            assertThat(response.getUserName()).isEqualTo(user.getUserName());
////            assertThat(response.getPostId()).isEqualTo(post.getId());
//
//            then(userRepository).should(times(1)).findByUserName(user.getUserName());
//            then(postRepository).should(times(1)).findById(post.getId());
//        }
    }

    @Nested
    @DisplayName("??????")
    class CommentUpdate {

        CommentRequest request = new CommentRequest("comment");

        @Test
        @DisplayName("?????? ?????? ??????")
        public void updateComment_success() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(user));
            given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

            CommentResponse response = commentService.update(post.getId(), comment.getId(), request, user.getUserName());

            assertThat(response.getId()).isEqualTo(comment.getId());
            assertThat(response.getComment()).isEqualTo(request.getComment());
            assertThat(response.getPostId()).isEqualTo(post.getId());
            assertThat(response.getUserName()).isEqualTo(user.getUserName());

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
            then(commentRepository).should(times(1)).findById(comment.getId());
        }

        @Test
        @DisplayName("?????? ?????? ?????? - ?????? ??????")
        public void updateComment_fail1() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class, () ->
                    commentService.update(post.getId(), comment.getId(), request, user.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.USERNAME_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("Not founded");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
        }

        @Test
        @DisplayName("?????? ?????? ?????? - ????????? ??????")
        public void updateComment_fail2() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(user));
            given(postRepository.findById(post.getId())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class, () ->
                    commentService.update(post.getId(), comment.getId(), request, user.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ???????????? ????????????.");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
        }

        @Test
        @DisplayName("?????? ?????? ?????? - ?????? ??????")
        public void updateComment_fail3() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(user));
            given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
            given(commentRepository.findById(comment.getId())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class, () ->
                    commentService.update(post.getId(), comment.getId(), request, user.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ????????????.");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
            then(commentRepository).should(times(1)).findById(comment.getId());

        }

        @Test
        @DisplayName("?????? ?????? ?????? - ?????? ?????? ????????? ?????? ???????????? ?????????")
        public void updateComment_fail4() {

            User user2 = User.builder().id(2).userName("user2").password("password2").build();

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(mockUser));
            given(postRepository.findById(post.getId())).willReturn(Optional.of(mockPost));
            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(mockComment));
            given(mockComment.getUser()).willReturn(user2);

            AppException appException = assertThrows(AppException.class, () ->
                    commentService.update(post.getId(), comment.getId(), request, user.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("???????????? ????????? ????????????.");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
            then(commentRepository).should(times(1)).findById(comment.getId());
        }

        @Test
        @DisplayName("?????? ?????? ?????? - ????????? ?????? ???????????? ?????? ????????? ???????????? ?????????")
        public void updateComment_fail5() {

            User user2 = User.builder().id(2).userName("user2").password("password2").build();
            Post post2 = Post.builder().id(2).title("title").body("body").user(user2).build();

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(mockUser));
            given(postRepository.findById(post.getId())).willReturn(Optional.of(mockPost));
            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(mockComment));
            given(mockComment.getUser()).willReturn(user);
            given(mockComment.getPost()).willReturn(post2);

            AppException appException = assertThrows(AppException.class, () ->
                    commentService.update(post.getId(), comment.getId(), request, user.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("???????????? ????????? ????????????.");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
            then(commentRepository).should(times(1)).findById(comment.getId());
        }
    }

    @Nested
    @DisplayName("??????")
    class DeleteComment {

        @Test
        @DisplayName("?????? ?????? ??????")
        public void deleteComment_success() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(user));
            given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

            CommentDeleteResponse response = commentService.delete(post.getId(), comment.getId(), user.getUserName());

            assertThat(response.getId()).isEqualTo(comment.getId());
            assertThat(response.getMessage()).isEqualTo("?????? ?????? ??????");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
            then(commentRepository).should(times(1)).findById(comment.getId());
        }

        @Test
        @DisplayName("?????? ?????? ?????? - ?????? ??????")
        public void deleteComment_fail1() {
            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class, () ->
                    commentService.delete(post.getId(), comment.getId(), user.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.USERNAME_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("Not founded");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
        }

        @Test
        @DisplayName("?????? ?????? ?????? - ????????? ??????")
        public void deleteComment_fail2() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(user));
            given(postRepository.findById(post.getId())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class, () ->
                    commentService.delete(post.getId(), comment.getId(), user.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.POST_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ???????????? ????????????.");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
        }

        @Test
        @DisplayName("?????? ?????? ?????? - ?????? ??????")
        public void deleteComment_fail3() {

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(user));
            given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
            given(commentRepository.findById(comment.getId())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class, () ->
                    commentService.delete(post.getId(), comment.getId(), user.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ????????????.");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
            then(commentRepository).should(times(1)).findById(comment.getId());
        }

        @Test
        @DisplayName("?????? ?????? ?????? - ?????? ?????? ????????? ?????? ???????????? ?????????")
        public void deleteComment_fail4() {

            User user2 = User.builder().id(2).userName("user2").password("password2").build();

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(mockUser));
            given(postRepository.findById(post.getId())).willReturn(Optional.of(mockPost));
            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(mockComment));
            given(mockComment.getUser()).willReturn(user2);

            AppException appException = assertThrows(AppException.class, () ->
                    commentService.delete(post.getId(), comment.getId(), user.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("???????????? ????????? ????????????.");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
            then(commentRepository).should(times(1)).findById(comment.getId());
        }

        @Test
        @DisplayName("?????? ?????? ?????? - ????????? ?????? ???????????? ?????? ????????? ???????????? ?????????")
        public void deleteComment_fail5() {

            User user2 = User.builder().id(2).userName("user2").password("password2").build();
            Post post2 = Post.builder().id(2).title("title").body("body").user(user2).build();

            given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(mockUser));
            given(postRepository.findById(post.getId())).willReturn(Optional.of(mockPost));
            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(mockComment));
            given(mockComment.getUser()).willReturn(user);
            given(mockComment.getPost()).willReturn(post2);

            AppException appException = assertThrows(AppException.class, () ->
                    commentService.delete(post.getId(), comment.getId(), user.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("???????????? ????????? ????????????.");

            then(userRepository).should(times(1)).findByUserName(user.getUserName());
            then(postRepository).should(times(1)).findById(post.getId());
            then(commentRepository).should(times(1)).findById(comment.getId());
        }

    }
}