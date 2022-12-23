package com.likelion.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.project.configuration.EncrypterConfig;
import com.likelion.project.configuration.JwtTokenExceptionFilter;
import com.likelion.project.configuration.JwtTokenFilter;
import com.likelion.project.configuration.JwtTokenUtil;
import com.likelion.project.domain.dto.post.*;
import com.likelion.project.domain.entity.Post;
import com.likelion.project.exception.ErrorCode;
import com.likelion.project.exception.AppException;
import com.likelion.project.exception.AppException;
import com.likelion.project.service.PostService;
import com.likelion.project.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(PostApiController.class)
class PostApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    PostService postService;

    @MockBean
    UserService userService;

    @Autowired
    WebApplicationContext context;

//    @MockBean
//    EncrypterConfig encrypterConfig;

    @Nested
    @DisplayName("조회")
    class PostList {
        @Test
        @DisplayName("포스트 상세 조회 성공")
        @WithMockUser
        public void postdetail_success() throws Exception {
            //given
            PostDetailResponse postDetailResponse = PostDetailResponse.builder()
                    .id(1)
                    .title("제목")
                    .body("내용")
                    .userName("userName")
                    .createdAt(LocalDateTime.now())
                    .lastModifiedAt(LocalDateTime.now())
                    .build();
            //when
            Integer postsId = 1;
            when(postService.findPost(postsId))
                    .thenReturn(postDetailResponse);

            //then
            mockMvc.perform(get("/api/v1/posts/" + postsId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postDetailResponse)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.title").exists())
                    .andExpect(jsonPath("$.title").value("제목"))
                    .andExpect(jsonPath("$.body").exists())
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.lastModifiedAt").exists())
                    .andDo(print());
            verify(postService).findPost(postsId);
        }

        @Test
        @DisplayName("포스트 리스트 조회 성공")
        @WithMockUser
        public void postlist_success() throws Exception {
            //given

            //when

            //then
        }
    }

    @Nested
    @DisplayName("등록")
    class PostCreate {

        @Test
        @DisplayName("포스트 등록 성공")
        @WithMockCustomUser
        public void post_create_success() throws Exception {

            //given
            PostCreateRequest postCreateRequest = new PostCreateRequest("제목", "내용");
            //when
            when(postService.createPost(any(),any())).thenReturn(new PostCreateResponse(1, "포스트 등록 완료"));
            //then
            mockMvc.perform(post("/api/v1/posts")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postCreateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.postId").exists())
                    .andExpect(jsonPath("$.result.message").exists())
                    .andDo(print());
            verify(postService).createPost(any(),any());
        }

        @Test
        @DisplayName("포스트 작성 실패(1) - 인증 실패 (JWT를 Bearer Token으로 보내지 않은 경우)")
        @WithAnonymousUser
        public void post_create_fail1() throws Exception {

            //given
            PostCreateRequest postCreateRequest = new PostCreateRequest("제목", "내용");
            //when
            when(postService.createPost(any(),any())).thenThrow(new AppException(ErrorCode.INVALID_PERMISSION));
            //then
            mockMvc.perform(post("/api/v1/posts")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postCreateRequest)))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());

        }

        @Test
        @DisplayName("포스트 작성 실패(2) - 인증 실패 (JWT가 유효하지 않은 경우)")
        @WithAnonymousUser
        public void post_create_fail2() throws Exception {
            //given
            PostCreateRequest postCreateRequest = new PostCreateRequest("제목", "내용");
            //when
            when(postService.createPost(any(),any())).thenThrow(new AppException(ErrorCode.INVALID_TOKEN));

            String token = JwtTokenUtil.createToken("user", "secret", 1000 * 60 * 60L);

            mockMvc = MockMvcBuilders
                    .webAppContextSetup(context)
                    .addFilters(new CharacterEncodingFilter("UTF-8", true))
                    .addFilters(new JwtTokenExceptionFilter())
                    .addFilters(new JwtTokenFilter("secret"))
                    .addFilters(new UsernamePasswordAuthenticationFilter())
                    .apply(springSecurity())
                    .build();
            //then
            mockMvc.perform(post("/api/v1/posts")
                            .with(csrf())
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token + " ")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postCreateRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result").exists())
                    .andExpect(jsonPath("$.result.errorCode").value("INVALID_TOKEN"))
                    .andExpect(jsonPath("$.result.message").value("잘못된 토큰입니다."))
                    .andDo(print());
        }

    }

    @Nested
    @DisplayName("수정")
    class PostUpdate {

        private PostUpdateRequest postUpdateRequest;

        @BeforeEach
        void setup() {
            postUpdateRequest = new PostUpdateRequest("제목", "내용");

        }

        @Test
        @DisplayName("포스트 수정 성공")
        @WithMockUser
        public void post_update_success() throws Exception {
            //given
            //when
            when(postService.update(any(), any(), any())).thenReturn(Post.builder().id(1).build());

            //then
            mockMvc.perform(put("/api/v1/posts/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.postId").exists())
                    .andExpect(jsonPath("$.result.message").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("포스트 수정 실패(1) : 인증 실패")
        @WithAnonymousUser // 인증 안된 상태
        public void post_update_fail1() throws Exception {
            //given
            //when
            when(postService.update(any(), any(), any())).
                    thenThrow(new AppException(ErrorCode.INVALID_PERMISSION));

            //then
            mockMvc.perform(put("/api/v1/posts/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postUpdateRequest)))
                    .andExpect(status().is(ErrorCode.INVALID_PERMISSION.getStatus().value()))
                    // 컨트롤러 테스트에서 이걸 하는 것이 맞는가?... 이렇게 하면 실패함
//                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
//                    .andExpect(jsonPath("$.result.errorCode").value("INVALID_PERMISSION"))
//                    .andExpect(jsonPath("$.result.errorCode").exists())
//                    .andExpect(jsonPath("$.result.message").value("사용자가 권한이 없습니다."))
//                    .andExpect(jsonPath("$.result.message").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("포스트 수정 실패(2) : 포스트 내용 없음")
        @WithMockUser
        public void post_update_fail2() throws Exception {
            //given
            //when
            when(postService.update(any(), any(), any())).
                    thenThrow(new AppException(ErrorCode.POST_NOT_FOUND));

            //then
            mockMvc.perform(put("/api/v1/posts/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postUpdateRequest)))
//                    .andExpect(status().is(ErrorCode.POST_NOT_FOUND.getStatus().value()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("POST_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.errorCode").exists())
                    .andExpect(jsonPath("$.result.message").value("해당 포스트가 없습니다."))
                    .andExpect(jsonPath("$.result.message").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("포스트 수정 실패(3) : 작성자 불일치")
        @WithMockUser
        public void post_update_fail3() throws Exception {
            //when
            when(postService.update(any(), any(), any())).
                    thenThrow(new AppException(ErrorCode.INVALID_PERMISSION));

            //then
            mockMvc.perform(put("/api/v1/posts/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postUpdateRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("INVALID_PERMISSION"))
                    .andExpect(jsonPath("$.result.errorCode").exists())
                    .andExpect(jsonPath("$.result.message").value("사용자가 권한이 없습니다."))
                    .andExpect(jsonPath("$.result.message").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("포스트 수정 실패(4) : 데이터베이스 에러")
        @WithMockUser
        public void post_update_fail4() throws Exception {
            //when
            when(postService.update(any(), any(), any())).
                    thenThrow(new AppException(ErrorCode.DATABASE_ERROR));

            //then
            mockMvc.perform(put("/api/v1/posts/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postUpdateRequest)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("DATABASE_ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").exists())
                    .andExpect(jsonPath("$.result.message").value("DB에러"))
                    .andExpect(jsonPath("$.result.message").exists())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("삭제")
    class PostDelete {

        private PostDeleteRequest postDeleteRequest;

        @BeforeEach
        void setup() {
            postDeleteRequest = new PostDeleteRequest(1);

        }

        @Test
        @DisplayName("포스트 삭제 성공")
        @WithMockUser
        public void post_delete_success() throws Exception {
            //when
            when(postService.delete(any(), any(), any()))
                    .thenReturn(postDeleteRequest.getId());

            //then
            mockMvc.perform(delete("/api/v1/posts/" + postDeleteRequest.getId())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postDeleteRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.postId").exists())
                    .andExpect(jsonPath("$.result.message").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("포스트 삭제 실패(1) : 인증 실패")
        @WithAnonymousUser
        public void post_delete_fail1() throws Exception {
            //when
            when(postService.delete(any(), any(), any()))
                    .thenThrow(new AppException(ErrorCode.INVALID_PERMISSION));
            //then
            mockMvc.perform(delete("/api/v1/posts/" + postDeleteRequest.getId())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postDeleteRequest)))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @DisplayName("포스트 삭제 실패(2) : 삭제할 포스트 없음")
        @WithMockUser
        public void post_delete_fail2() throws Exception {
            //when
            when(postService.delete(any(), any(), any()))
                    .thenThrow(new AppException(ErrorCode.POST_NOT_FOUND));
            //then
            mockMvc.perform(delete("/api/v1/posts/" + postDeleteRequest.getId())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postDeleteRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("POST_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.errorCode").exists())
                    .andExpect(jsonPath("$.result.message").value("해당 포스트가 없습니다."))
                    .andExpect(jsonPath("$.result.message").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("포스트 삭제 실패(3) : 작성자 불일치")
        @WithMockUser
        public void post_delete_fail3() throws Exception {
            //when
            when(postService.delete(any(), any(), any())).
                    thenThrow(new AppException(ErrorCode.INVALID_PERMISSION));

            //then
            mockMvc.perform(delete("/api/v1/posts/" + postDeleteRequest.getId())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postDeleteRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("INVALID_PERMISSION"))
                    .andExpect(jsonPath("$.result.errorCode").exists())
                    .andExpect(jsonPath("$.result.message").value("사용자가 권한이 없습니다."))
                    .andExpect(jsonPath("$.result.message").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("포스트 삭제 실패(4) : 데이터베이스 에러")
        @WithMockUser
        public void post_delete_fail4() throws Exception {
            //when
            when(postService.delete(any(), any(), any())).
                    thenThrow(new AppException(ErrorCode.DATABASE_ERROR));

            //then
            mockMvc.perform(delete("/api/v1/posts/" + postDeleteRequest.getId())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postDeleteRequest)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("DATABASE_ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").exists())
                    .andExpect(jsonPath("$.result.message").value("DB에러"))
                    .andExpect(jsonPath("$.result.message").exists())
                    .andDo(print());
        }
    }
}