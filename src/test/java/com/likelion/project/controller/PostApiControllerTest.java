package com.likelion.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.project.annotation.WebMvcTestSecurity;
import com.likelion.project.domain.dto.post.*;
import com.likelion.project.exception.AppException;
import com.likelion.project.exception.ErrorCode;
import com.likelion.project.jwt.JwtTokenUtil;
import com.likelion.project.service.PostService;
import com.likelion.project.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static com.likelion.project.exception.ErrorCode.INVALID_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTestSecurity(value = PostApiController.class)
//@WebAppConfiguration
//@ContextConfiguration(classes = SecurityConfig.class)
class PostApiControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    PostService postService;
    @MockBean
    UserService userService;
    @Value("${jwt.token.secret}") String secretKey;

    private final PostCreateRequest postCreateRequest = new PostCreateRequest("제목", "내용");
    private final PostCreateResponse postCreateResponse = new PostCreateResponse(1, "포스트 등록 완료");
    private final PostResponse postResponse1 = new PostResponse(1, "title", "body", "user1", LocalDateTime.now(), LocalDateTime.now());
    private final PostResponse postResponse2 = new PostResponse(2, "title", "body", "user2", LocalDateTime.now(), LocalDateTime.now());
    private final PageImpl<PostResponse> user1page = new PageImpl<>(List.of(postResponse1));
    private final PageImpl<PostResponse> page = new PageImpl<>(List.of(postResponse1, postResponse2));
    private final PageRequest pageable = PageRequest.of(0, 20,Sort.Direction.DESC,"registeredAt");
    private PostUpdateRequest postUpdateRequest;
    private PostDeleteRequest postDeleteRequest;
    private final PostDeleteResponse postDeleteResponse = new PostDeleteResponse(1, "포스트 삭제 완료");
    private final PostUpdateResponse postUpdateResponse = new PostUpdateResponse(1, "포스트 수정 완료");
    private final PostDetailResponse postDetailResponse = PostDetailResponse.builder().id(1).title("title").body("body").userName("userName").build();

    @Nested
    @DisplayName("조회")
    class PostList {
        @Test
        @DisplayName("포스트 상세 조회 성공")
        public void postdetail_success() throws Exception {

            Integer postsId = 1;
            given(postService.findPost(postsId)).willReturn(postDetailResponse);

            mockMvc.perform(get("/api/v1/posts/" + postsId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value(1))
                    .andExpect(jsonPath("$.result.title").value("title"))
                    .andExpect(jsonPath("$.result.body").value("body"))
                    .andDo(print());

            then(postService).should(times(1)).findPost(postsId);
        }

        @Test
        @DisplayName("포스트 리스트 조회 성공 - 0번이 1번보다 날짜가 최신")
        public void postlist_success() throws Exception {

            given(postService.findAllPost(pageable)).willReturn(page);

            mockMvc.perform(get("/api/v1/posts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.content").isArray())
                    .andExpect(jsonPath("$['result']['content'][0]").exists())
                    .andExpect(jsonPath("$['result']['content'][1]").exists())
                    .andExpect(jsonPath("$.result.pageable").exists())
                    .andExpect(jsonPath("$.result.size").value(2))
                    .andExpect(jsonPath("$.result.sort").exists())
                    .andDo(print());

            assertThat(pageable.getPageNumber()).isEqualTo(0);
            assertThat(pageable.getPageSize()).isEqualTo(20);
            assertThat(pageable.getSort()).isEqualTo(Sort.by("registeredAt").descending());

            then(postService).should(times(1)).findAllPost(pageable);
        }

        @Test
        @DisplayName("마이 피드 조회 성공")
        public void mypost_success() throws Exception {

            String token = JwtTokenUtil.createToken("user1", secretKey, 1000 * 60 * 60L);

            given(postService.findMyPost(postResponse1.getUserName(), pageable)).willReturn(user1page);

            mockMvc.perform(get("/api/v1/posts/my")
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.content").isArray())
                    .andExpect(jsonPath("$['result']['content'][0]").exists())
                    .andExpect(jsonPath("$.result.pageable").exists())
                    .andExpect(jsonPath("$.result.size").value(1))
                    .andExpect(jsonPath("$.result.sort").exists())
                    .andDo(print());

            assertThat(pageable.getPageNumber()).isEqualTo(0);
            assertThat(pageable.getPageSize()).isEqualTo(20);
            assertThat(pageable.getSort()).isEqualTo(Sort.by("registeredAt").descending());

            then(postService).should(times(1)).findMyPost(postResponse1.getUserName(),pageable);
        }

        @Test
        @DisplayName("마이피드 조회 실패(1) - 로그인 하지 않은 경우")
        public void mypost_fail1() throws Exception {

            mockMvc.perform(get("/api/v1/posts/my"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("TOKEN_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.message").value("토큰이 존재하지 않습니다."))
                    .andDo(print());

            then(postService).should(never()).findMyPost(any(),any());
        }

        @Test
        @DisplayName("좋아요 갯수 조회 성공")
        public void getCountLike_success() throws Exception {

            Integer postId = 1;
            given(postService.getCountLike(postId)).willReturn(anyLong());

            mockMvc.perform(get("/api/v1/posts/" + postId + "/likes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result").value(0))
                    .andDo(print());

            then(postService).should(times(1)).getCountLike(postId);
        }

        @Test
        @DisplayName("좋아요 갯수 조회 실패 - 포스트 없음")
        public void getCountLike_fail1() throws Exception {

            Integer postId = 1;
            given(postService.getCountLike(postId)).willThrow(new AppException(ErrorCode.POST_NOT_FOUND));

            mockMvc.perform(get("/api/v1/posts/" + postId + "/likes"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("POST_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.message").value("해당 포스트가 없습니다."))
                    .andDo(print());

            then(postService).should(times(1)).getCountLike(postId);
        }
    }

    @Nested
    @DisplayName("등록")
    class PostCreate {

        @Test
        @DisplayName("포스트 등록 성공")
        public void post_create_success() throws Exception {

            String token = JwtTokenUtil.createToken("userName", secretKey, 1000 * 60 * 60L);

            given(postService.createPost(any(),any())).willReturn(postCreateResponse);

//            mockMvc = MockMvcBuilders
//                    .webAppContextSetup(context)
//                    .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 한글 깨짐 방지 필터
//                    .apply(springSecurity())
//                    .build();

            mockMvc.perform(post("/api/v1/posts")
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postCreateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.postId").value(1))
                    .andExpect(jsonPath("$.result.message").value("포스트 등록 완료"))
                    .andDo(print());

            then(postService).should(times(1)).createPost(any(),any());
        }

        @Test
        @DisplayName("포스트 작성 실패(1) - 인증 실패 (Bearer 토큰으로 보내지 않은 경우)")
        public void post_create_fail1() throws Exception {

            String token = JwtTokenUtil.createToken("user", secretKey, 1000 * 60 * 60L);

//            mockMvc = MockMvcBuilders
//                    .webAppContextSetup(context)
//                    .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 한글 깨짐 방지 필터
//                    .addFilters(new JwtTokenExceptionFilter())
//                    .addFilters(new JwtTokenFilter(secretKey))
//                    .addFilters(new UsernamePasswordAuthenticationFilter())
//                    .apply(springSecurity())
//                    .build();

            mockMvc.perform(post("/api/v1/posts")
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
            String token = JwtTokenUtil.createToken("user", secretKey, 1L);

//            mockMvc = MockMvcBuilders
//                    .webAppContextSetup(context)
//                    .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 한글 깨짐 방지 필터
//                    .addFilters(new JwtTokenExceptionFilter())
//                    .addFilters(new JwtTokenFilter(secretKey))
//                    .addFilters(new UsernamePasswordAuthenticationFilter())
//                    .apply(springSecurity())
//                    .build();

            mockMvc.perform(post("/api/v1/posts")
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

        @Test
        @DisplayName("좋아요 누르기 성공")
        public void like_create_success() throws Exception {

            Integer postId = 1;
            String token = JwtTokenUtil.createToken("userName", secretKey, 1000 * 60 * 60L);

            given(postService.createLike(any(), any())).willReturn("좋아요를 눌렀습니다.");

            mockMvc.perform(post("/api/v1/posts/" + postId + "/likes")
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result").value("좋아요를 눌렀습니다."))
                    .andDo(print());

            then(postService).should(times(1)).createLike(any(),any());
        }

        @Test
        @DisplayName("좋아요 누르기 실패(1) - 로그인 하지 않은 경우")
        public void like_create_fail1() throws Exception {

            Integer postId = 1;

            mockMvc.perform(post("/api/v1/posts/" + postId + "/likes"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("TOKEN_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.message").value("토큰이 존재하지 않습니다."))
                    .andDo(print());

            then(postService).should(never()).createLike(any(),any());
        }

        @Test
        @DisplayName("좋아요 누르기 실패(2) - 해당 Post가 없는 경우")
        public void like_create_fail2() throws Exception {

            Integer postId = 1;
            String token = JwtTokenUtil.createToken("userName", secretKey, 1000 * 60 * 60L);

            given(postService.createLike(any(), any())).willThrow(new AppException(ErrorCode.POST_NOT_FOUND));

            mockMvc.perform(post("/api/v1/posts/" + postId + "/likes")
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("POST_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.message").value("해당 포스트가 없습니다."))
                    .andDo(print());

            then(postService).should(times(1)).createLike(any(),any());
        }
    }

    @Nested
    @DisplayName("수정")
    class PostUpdate {

        String token = JwtTokenUtil.createToken("user", secretKey, 1000 * 60 * 60L);

        @BeforeEach
        void setup() {
            postUpdateRequest = new PostUpdateRequest("제목", "내용");
        }

        @Test
        @DisplayName("포스트 수정 성공")
        public void post_update_success() throws Exception {

            given(postService.update(any(), any(), any())).willReturn(postUpdateResponse);

            mockMvc.perform(put("/api/v1/posts/1")
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.postId").value(1))
                    .andExpect(jsonPath("$.result.message").value("포스트 수정 완료"))
                    .andDo(print());

            then(postService).should(times(1)).update(any(), any(), any());
        }

        @Test
        @DisplayName("포스트 수정 실패(1) : 인증 실패")
        public void post_update_fail1() throws Exception {
            // 일부러 구성이 올바르지 않은 토큰을 집어넣음
            // 어쨌든 통합적으로 인증 실패 테스트이니 토큰이 이상하기만 한 상황을 가정했다.
            mockMvc.perform(put("/api/v1/posts/1")
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
        public void post_update_fail2() throws Exception {

            willThrow(new AppException(ErrorCode.POST_NOT_FOUND)).given(postService).update(any(),any(), any());

            mockMvc.perform(put("/api/v1/posts/1")
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postUpdateRequest)))
                    .andExpect(status().is(ErrorCode.POST_NOT_FOUND.getStatus().value()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("POST_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.message").value("해당 포스트가 없습니다."))
                    .andDo(print());

            then(postService).should(times(1)).update(any(), any(), any());
        }

        @Test
        @DisplayName("포스트 수정 실패(3) : 작성자 불일치")
        public void post_update_fail3() throws Exception {

            willThrow(new AppException(ErrorCode.INVALID_PERMISSION)).given(postService).update(any(),any(), any());

            mockMvc.perform(put("/api/v1/posts/1")
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postUpdateRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("INVALID_PERMISSION"))
                    .andExpect(jsonPath("$.result.message").value("사용자가 권한이 없습니다."))
                    .andDo(print());

            then(postService).should(times(1)).update(any(), any(), any());
        }

        @Test
        @DisplayName("포스트 수정 실패(4) : 데이터베이스 에러")
        public void post_update_fail4() throws Exception {

            willThrow(new AppException(ErrorCode.DATABASE_ERROR)).given(postService).update(any(),any(), any());

            mockMvc.perform(put("/api/v1/posts/1")
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postUpdateRequest)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("DATABASE_ERROR"))
                    .andExpect(jsonPath("$.result.message").value("DB에러"))
                    .andDo(print());

            then(postService).should(times(1)).update(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("삭제")
    class PostDelete {

        String token = JwtTokenUtil.createToken("user", secretKey, 1000 * 60 * 60L);
        @BeforeEach
        void setup() {
            postDeleteRequest = new PostDeleteRequest(1);
        }

        @Test
        @DisplayName("포스트 삭제 성공")
        public void post_delete_success() throws Exception {

            given(postService.delete(any(), any())).willReturn(postDeleteResponse);

            mockMvc.perform(delete("/api/v1/posts/" + postDeleteRequest.getId())
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postDeleteRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.message").value("포스트 삭제 완료"))
                    .andExpect(jsonPath("$.result.postId").value(1))
                    .andDo(print());

            then(postService).should(times(1)).delete(any(), any());
        }

        @Test
        @DisplayName("포스트 삭제 실패(1) : 인증 실패")
        public void post_delete_fail1() throws Exception {
            // 이번엔 서명을 확인할 수 없는 토큰으로 테스트 해보겠다.
            // 이 또한, 어쨌든 통합적으로 인증 실패 테스트이니 토큰이 이상하기만 한 상황을 가정했다.
            mockMvc.perform(delete("/api/v1/posts/" + postDeleteRequest.getId())
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
        public void post_delete_fail2() throws Exception {

            willThrow(new AppException(ErrorCode.POST_NOT_FOUND)).given(postService).delete(any(), any());

            mockMvc.perform(delete("/api/v1/posts/" + postDeleteRequest.getId())
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postDeleteRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("POST_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.message").value("해당 포스트가 없습니다."))
                    .andDo(print());

            then(postService).should(times(1)).delete(any(), any());
        }

        @Test
        @DisplayName("포스트 삭제 실패(3) : 작성자 불일치")
        public void post_delete_fail3() throws Exception {

            willThrow(new AppException(ErrorCode.INVALID_PERMISSION)).given(postService).delete(any(), any());

            mockMvc.perform(delete("/api/v1/posts/" + postDeleteRequest.getId())
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postDeleteRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("INVALID_PERMISSION"))
                    .andExpect(jsonPath("$.result.message").value("사용자가 권한이 없습니다."))
                    .andDo(print());

            then(postService).should(times(1)).delete(any(), any());
        }

        @Test
        @DisplayName("포스트 삭제 실패(4) : 데이터베이스 에러")
        public void post_delete_fail4() throws Exception {

            willThrow(new AppException(ErrorCode.DATABASE_ERROR)).given(postService).delete(any(), any());

            mockMvc.perform(delete("/api/v1/posts/" + postDeleteRequest.getId())
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(postDeleteRequest)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("DATABASE_ERROR"))
                    .andExpect(jsonPath("$.result.message").value("DB에러"))
                    .andDo(print());

            then(postService).should(times(1)).delete(any(), any());
        }
    }
}
