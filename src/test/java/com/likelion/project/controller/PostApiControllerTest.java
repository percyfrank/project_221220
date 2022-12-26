package com.likelion.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.project.jwt.JwtTokenExceptionFilter;
import com.likelion.project.jwt.JwtTokenFilter;
import com.likelion.project.jwt.JwtTokenUtil;
import com.likelion.project.domain.dto.post.*;
import com.likelion.project.domain.entity.Post;
import com.likelion.project.exception.ErrorCode;
import com.likelion.project.exception.AppException;
import com.likelion.project.service.PostService;
import com.likelion.project.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static com.likelion.project.exception.ErrorCode.INVALID_TOKEN;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

    private final PostCreateRequest postCreateRequest = new PostCreateRequest("제목", "내용");
    private final PostCreateResponse postCreateResponse = new PostCreateResponse(1, "포스트 등록 완료");

    private PostUpdateRequest postUpdateRequest;

    private PostDeleteRequest postDeleteRequest;

    private final Post post = Post.builder().id(1).build();

    private final PostDetailResponse postDetailResponse = PostDetailResponse.builder().id(1).title("title").body("body").userName("userName").build();

    @Nested
    @DisplayName("조회")
    class PostList {
        @Test
        @DisplayName("포스트 상세 조회 성공")
        @WithMockCustomUser // 비회원도 볼 수 있게는 아직 실패...
        public void postdetail_success() throws Exception {
            Integer postsId = 1;
            given(postService.findPost(postsId)).willReturn(postDetailResponse);

            mockMvc.perform(get("/api/v1/posts/" + postsId))
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsBytes(postDetailResponse)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("title"))
                    .andExpect(jsonPath("$.body").value("body"))
                    .andDo(print());

            verify(postService,times(1)).findPost(postsId);
        }

        @Test
        @DisplayName("포스트 리스트 조회 성공 - 0번이 1번보다 날짜가 최신")
        @WithMockUser
        public void postlist_success() throws Exception {

            PageRequest pageable = PageRequest.of(0, 20,Sort.Direction.DESC,"id");

            mockMvc.perform(get("/api/v1/posts")
                            .param("page", "0")
                            .param("size", "20")
                            .param("sort", "id")
                            .param("direction","Sort.Direction.DESC"))
                    .andExpect(status().isOk());

            assertThat(pageable.getPageNumber()).isEqualTo(0);
            assertThat(pageable.getPageSize()).isEqualTo(20);
            assertThat(pageable.getSort()).isEqualTo(Sort.by("id").descending());
        }
    }

    @Nested
    @DisplayName("등록")
    class PostCreate {

        @Test
        @DisplayName("포스트 등록 성공")
        @WithMockCustomUser
        public void post_create_success() throws Exception {

            given(postService.createPost(any(),any())).willReturn(postCreateResponse);

            mockMvc.perform(post("/api/v1/posts")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postCreateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.postId").value(1))
                    .andExpect(jsonPath("$.result.message").value("포스트 등록 완료"))
                    .andDo(print());

            verify(postService,times(1)).createPost(any(),any());
        }

        @Test
        @DisplayName("포스트 작성 실패(1) - 인증 실패 (Bearer 토큰으로 보내지 않은 경우)")
        public void post_create_fail1() throws Exception {

            String token = JwtTokenUtil.createToken("user", "secretKey", 1000 * 60 * 60L);

            mockMvc = MockMvcBuilders
                    .webAppContextSetup(context)
                    .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 한글 깨짐 방지 필터
                    .addFilters(new JwtTokenExceptionFilter())
                    .addFilters(new JwtTokenFilter("secretKey"))
                    .addFilters(new UsernamePasswordAuthenticationFilter())
                    .apply(springSecurity())
                    .build();

            mockMvc.perform(post("/api/v1/posts")
                            .with(csrf())
                            .header(HttpHeaders.AUTHORIZATION,"Basic " + token) // Basic으로 보냄
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postCreateRequest)))
                    .andExpect(status().is(INVALID_TOKEN.getStatus().value()))
                    .andDo(print());
        }

        @Test
        @DisplayName("포스트 작성 실패(2) - 인증 실패 (JWT가 유효하지 않은 경우)")
        public void post_create_fail2() throws Exception {

            // 나의 경우, 토큰이 유효하지 않은 경우는 다음과 같다. JwtTokenExceptionFilter 참고하기 바람
            // 만료된 토큰, 구성이 올바르지 못한 토큰, 서명을 확인할 수 없는 토큰, 지원하지 않는 형식의 토큰 등의 경우가 있다.
            // 포스트 작성, 수정, 삭제 실패 테스트에서 각각의 경우를 차례로 테스트해서 인증 실패 테스르를 할 것이다.
            // 원래는 테스트마다 모든 경우를 다 해봐야하겠지만, 생략한다.

            // 만료된 토큰을 생성한다.
            String token = JwtTokenUtil.createToken("user", "secretKey", 1L);

            mockMvc = MockMvcBuilders
                    .webAppContextSetup(context)
                    .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 한글 깨짐 방지 필터
                    .addFilters(new JwtTokenExceptionFilter())
                    .addFilters(new JwtTokenFilter("secretKey"))
                    .addFilters(new UsernamePasswordAuthenticationFilter())
                    .apply(springSecurity())
                    .build();

            mockMvc.perform(post("/api/v1/posts")
                            .with(csrf())
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token)
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

        @BeforeEach
        void setup() {
            postUpdateRequest = new PostUpdateRequest("제목", "내용");
        }

        @Test
        @DisplayName("포스트 수정 성공")
        @WithMockCustomUser
        public void post_update_success() throws Exception {

            given(postService.update(any(), any(), any())).willReturn(post);

            mockMvc.perform(put("/api/v1/posts/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.postId").value(1))
                    .andExpect(jsonPath("$.result.message").value("포스트 수정 완료"))
                    .andDo(print());

            verify(postService, times(1)).update(any(), any(), any());
        }

        @Test
        @DisplayName("포스트 수정 실패(1) : 인증 실패")
        public void post_update_fail1() throws Exception {
            // 일부러 구성이 올바르지 않은 토큰을 집어넣음
            // 어쨌든 통합적으로 인증 실패 테스트이니 토큰이 이상하기만 한 상황을 가정했다.
            mockMvc = MockMvcBuilders
                    .webAppContextSetup(context)
                    .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 한글 깨짐 방지 필터
                    .addFilters(new JwtTokenExceptionFilter())
                    .addFilters(new JwtTokenFilter("secretKey"))
                    .addFilters(new UsernamePasswordAuthenticationFilter())
                    .apply(springSecurity())
                    .build();

            mockMvc.perform(put("/api/v1/posts/1")
                            .with(csrf())
                            .header(HttpHeaders.AUTHORIZATION,"Bearer a.b.c")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postUpdateRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("INVALID_TOKEN"))
                    .andExpect(jsonPath("$.result.message").value("잘못된 토큰입니다."))
                    .andDo(print());
        }

        @Test
        @DisplayName("포스트 수정 실패(2) : 포스트 내용 없음")
        @WithMockCustomUser
        public void post_update_fail2() throws Exception {

            given(postService.update(any(), any(), any())).willThrow(new AppException(ErrorCode.POST_NOT_FOUND));

            mockMvc.perform(put("/api/v1/posts/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postUpdateRequest)))
                    .andExpect(status().is(ErrorCode.POST_NOT_FOUND.getStatus().value()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("POST_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.message").value("해당 포스트가 없습니다."))
                    .andDo(print());

            verify(postService, times(1)).update(any(), any(), any());
        }

        @Test
        @DisplayName("포스트 수정 실패(3) : 작성자 불일치")
        @WithMockCustomUser
        public void post_update_fail3() throws Exception {

            given(postService.update(any(), any(), any())).willThrow(new AppException(ErrorCode.INVALID_PERMISSION));

            mockMvc.perform(put("/api/v1/posts/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postUpdateRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("INVALID_PERMISSION"))
                    .andExpect(jsonPath("$.result.message").value("사용자가 권한이 없습니다."))
                    .andDo(print());

            verify(postService, times(1)).update(any(), any(), any());
        }

        @Test
        @DisplayName("포스트 수정 실패(4) : 데이터베이스 에러")
        @WithMockUser
        public void post_update_fail4() throws Exception {

            given(postService.update(any(), any(), any())).willThrow(new AppException(ErrorCode.DATABASE_ERROR));

            mockMvc.perform(put("/api/v1/posts/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postUpdateRequest)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("DATABASE_ERROR"))
                    .andExpect(jsonPath("$.result.message").value("DB에러"))
                    .andDo(print());

            verify(postService, times(1)).update(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("삭제")
    class PostDelete {

        @BeforeEach
        void setup() {
            postDeleteRequest = new PostDeleteRequest(1);
        }

        @Test
        @DisplayName("포스트 삭제 성공")
        @WithMockCustomUser
        public void post_delete_success() throws Exception {

            given(postService.delete(any(), any())).willReturn(postDeleteRequest.getId());

            mockMvc.perform(delete("/api/v1/posts/" + postDeleteRequest.getId())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postDeleteRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.message").value("포스트 삭제 완료"))
                    .andExpect(jsonPath("$.result.postId").value(1))
                    .andDo(print());

            verify(postService, times(1)).delete(any(), any());
        }

        @Test
        @DisplayName("포스트 삭제 실패(1) : 인증 실패")
        public void post_delete_fail1() throws Exception {
            // 이번엔 서명을 확인할 수 없는 토큰으로 테스트 해보겠다.
            // 이 또한, 어쨌든 통합적으로 인증 실패 테스트이니 토큰이 이상하기만 한 상황을 가정했다.
            String token = JwtTokenUtil.createToken("user", "secretKey", 1000 * 60 * 60L);
            
            mockMvc = MockMvcBuilders
                    .webAppContextSetup(context)
                    .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 한글 깨짐 방지 필터
                    .addFilters(new JwtTokenExceptionFilter())
                    .addFilters(new JwtTokenFilter("secret"))
                    .addFilters(new UsernamePasswordAuthenticationFilter())
                    .apply(springSecurity())
                    .build();

            mockMvc.perform(delete("/api/v1/posts/" + postDeleteRequest.getId())
                            .with(csrf())
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token + "wrong")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postDeleteRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("INVALID_TOKEN"))
                    .andExpect(jsonPath("$.result.message").value("잘못된 토큰입니다."))
                    .andDo(print());
        }

        @Test
        @DisplayName("포스트 삭제 실패(2) : 삭제할 포스트 없음")
        @WithMockCustomUser
        public void post_delete_fail2() throws Exception {
            given(postService.delete(any(), any())).willThrow(new AppException(ErrorCode.POST_NOT_FOUND));

            mockMvc.perform(delete("/api/v1/posts/" + postDeleteRequest.getId())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postDeleteRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("POST_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.message").value("해당 포스트가 없습니다."))
                    .andDo(print());

            verify(postService, times(1)).delete(any(), any());
        }

        @Test
        @DisplayName("포스트 삭제 실패(3) : 작성자 불일치")
        @WithMockCustomUser
        public void post_delete_fail3() throws Exception {

            given(postService.delete(any(), any())).willThrow(new AppException(ErrorCode.INVALID_PERMISSION));

            mockMvc.perform(delete("/api/v1/posts/" + postDeleteRequest.getId())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postDeleteRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("INVALID_PERMISSION"))
                    .andExpect(jsonPath("$.result.message").value("사용자가 권한이 없습니다."))
                    .andDo(print());

            verify(postService, times(1)).delete(any(), any());
        }

        @Test
        @DisplayName("포스트 삭제 실패(4) : 데이터베이스 에러")
        @WithMockCustomUser
        public void post_delete_fail4() throws Exception {

            given(postService.delete(any(), any())).willThrow(new AppException(ErrorCode.DATABASE_ERROR));

            mockMvc.perform(delete("/api/v1/posts/" + postDeleteRequest.getId())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postDeleteRequest)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("DATABASE_ERROR"))
                    .andExpect(jsonPath("$.result.message").value("DB에러"))
                    .andDo(print());

            verify(postService, times(1)).delete(any(), any());
        }
    }
}