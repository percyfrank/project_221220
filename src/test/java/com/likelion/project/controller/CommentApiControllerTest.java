package com.likelion.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.project.annotation.WebMvcTestSecurity;
import com.likelion.project.domain.dto.comment.CommentDeleteResponse;
import com.likelion.project.domain.dto.comment.CommentRequest;
import com.likelion.project.domain.dto.comment.CommentResponse;
import com.likelion.project.exception.AppException;
import com.likelion.project.exception.ErrorCode;
import com.likelion.project.jwt.JwtTokenUtil;
import com.likelion.project.service.CommentService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTestSecurity(value = CommentApiController.class)
//@WebAppConfiguration
class CommentApiControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    CommentService commentService;
    @Value("${jwt.token.secret}") String secretKey;

    private final CommentRequest commentRequest = new CommentRequest("comment");
    private final CommentResponse commentResponse = new CommentResponse(1, "comment", "userName", 1, LocalDateTime.now(),LocalDateTime.now());
    private final CommentResponse commentResponse2 = new CommentResponse(2, "comment2", "userName2", 1, LocalDateTime.now(), LocalDateTime.now());
    private final PageImpl<CommentResponse> page = new PageImpl<>(List.of(commentResponse,commentResponse2));
    private final PageRequest pageable = PageRequest.of(0, 10,Sort.Direction.DESC,"registeredAt");
    private final CommentDeleteResponse commentDeleteResponse = new CommentDeleteResponse(1, "?????? ?????? ??????");


    @Nested
    @DisplayName("??????")
    class CommentList {

        @Test
        @DisplayName("?????? ?????? ?????? ??????")
        public void comment_list_success() throws Exception {

            Integer postId = 1;
            given(commentService.findAllComments(postId, pageable)).willReturn(page);

            mockMvc.perform(get("/api/v1/posts/" + postId + "/comments"))
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
            assertThat(pageable.getPageSize()).isEqualTo(10);
            assertThat(pageable.getSort()).isEqualTo(Sort.by("registeredAt").descending());

            then(commentService).should(times(1)).findAllComments(postId,pageable);
        }
    }

    @Nested
    @DisplayName("??????")
    class CommentCreate {

        @Test
        @DisplayName("?????? ?????? ??????")
        public void comment_create_success() throws Exception {

            String token = JwtTokenUtil.createToken("userName", secretKey, 1000 * 60 * 60L);

            given(commentService.create(any(), any(), any())).willReturn(commentResponse);

            mockMvc.perform(post("/api/v1/posts/" + commentResponse.getPostId() + "/comments")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(commentRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.id").value(1))
                    .andExpect(jsonPath("$.result.comment").value("comment"))
                    .andExpect(jsonPath("$.result.userName").value("userName"))
                    .andExpect(jsonPath("$.result.postId").value(1))
                    .andDo(print());

            then(commentService).should(times(1)).create(any(), any(), any());

        }

        @Test
        @DisplayName("?????? ?????? ??????(1) - ????????? ?????? ?????? ??????")
        public void comment_create_fail1() throws Exception {

            mockMvc.perform(post("/api/v1/posts/" + commentResponse.getPostId() + "/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(commentRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("TOKEN_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.message").value("????????? ???????????? ????????????."))
                    .andDo(print());

            then(commentService).should(never()).create(any(), any(), any());

        }

        @Test
        @DisplayName("?????? ?????? ??????(2) - ???????????? ???????????? ?????? ??????")
        public void comment_create_fail2() throws Exception {

            String token = JwtTokenUtil.createToken("userName", secretKey, 1000 * 60 * 60L);

            given(commentService.create(any(), any(), any())).willThrow(new AppException(ErrorCode.POST_NOT_FOUND));

            mockMvc.perform(post("/api/v1/posts/" + commentResponse.getPostId() + "/comments")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(commentRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("POST_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.message").value("?????? ???????????? ????????????."))
                    .andDo(print());

            then(commentService).should(times(1)).create(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("??????")
    class CommentUpdate {

        String token = JwtTokenUtil.createToken("user", secretKey, 1000 * 60 * 60L);

        @Test
        @DisplayName("?????? ?????? ??????")
        public void comment_update_success() throws Exception {

            given(commentService.update(any(), any(), any(), any())).willReturn(commentResponse);

            mockMvc.perform(put("/api/v1/posts/" + commentResponse.getPostId() + "/comments/" + commentResponse.getId())
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(commentRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.id").value(1))
                    .andExpect(jsonPath("$.result.comment").value("comment"))
                    .andExpect(jsonPath("$.result.userName").value("userName"))
                    .andExpect(jsonPath("$.result.postId").value(1))
                    .andExpect(jsonPath("$.result.createdAt").exists())
                    .andExpect(jsonPath("$.result.lastModifiedAt").exists())
                    .andDo(print());

            then(commentService).should(times(1)).update(any(), any(), any(), any());

        }

        @Test
        @DisplayName("?????? ?????? ??????(1) : ?????? ??????")
        public void comment_update_fail1() throws Exception {

            mockMvc.perform(put("/api/v1/posts/" + commentResponse.getPostId() + "/comments/" + commentResponse.getId())
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token + "abc")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(commentRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("INVALID_TOKEN"))
                    .andExpect(jsonPath("$.result.message").value("????????? ???????????????."))
                    .andDo(print());

            then(commentService).should(never()).update(any(), any(), any(), any());
        }

        @Test
        @DisplayName("?????? ?????? ??????(2) : Post ?????? ??????")
        public void comment_update_fail2() throws Exception {

            given(commentService.update(any(), any(), any(), any())).willThrow(new AppException(ErrorCode.POST_NOT_FOUND));

            mockMvc.perform(put("/api/v1/posts/" + commentResponse.getPostId() + "/comments/" + commentResponse.getId())
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(commentRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("POST_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.message").value("?????? ???????????? ????????????."))
                    .andDo(print());

            then(commentService).should(times(1)).update(any(), any(), any(), any());
        }

        @Test
        @DisplayName("?????? ?????? ??????(3) : ????????? ?????????")
        public void comment_update_fail3() throws Exception {

            given(commentService.update(any(), any(), any(), any())).willThrow(new AppException(ErrorCode.POST_NOT_FOUND));

            mockMvc.perform(put("/api/v1/posts/" + commentResponse.getPostId() + "/comments/" + commentResponse.getId())
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(commentRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("POST_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.message").value("?????? ???????????? ????????????."))
                    .andDo(print());

            then(commentService).should(times(1)).update(any(), any(), any(), any());
        }

        @Test
        @DisplayName("?????? ?????? ??????(4) : ?????????????????? ??????")
        public void comment_update_fail4() throws Exception {

            given(commentService.update(any(), any(), any(), any())).willThrow(new AppException(ErrorCode.DATABASE_ERROR));

            mockMvc.perform(put("/api/v1/posts/" + commentResponse.getPostId() + "/comments/" + commentResponse.getId())
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(commentRequest)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("DATABASE_ERROR"))
                    .andExpect(jsonPath("$.result.message").value("DB??????"))
                    .andDo(print());

            then(commentService).should(times(1)).update(any(), any(), any(), any());
        }

    }

    @Nested
    @DisplayName("??????")
    class CommentDelete {

        String token = JwtTokenUtil.createToken("user", secretKey, 1000 * 60 * 60L);
        Integer postId = 1;

        @Test
        @DisplayName("?????? ?????? ??????")
        public void comment_delete_success() throws Exception {

            given(commentService.delete(any(), any(), any())).willReturn(commentDeleteResponse);

            mockMvc.perform(delete("/api/v1/posts/" + postId + "/comments/" + commentDeleteResponse.getId())
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.id").value(1))
                    .andExpect(jsonPath("$.result.message").value("?????? ?????? ??????"))
                    .andDo(print());

            then(commentService).should(times(1)).delete(any(), any(), any());
        }

        @Test
        @DisplayName("?????? ?????? ??????(1) : ?????? ??????")
        public void comment_delete_fail1() throws Exception {

            mockMvc.perform(delete("/api/v1/posts/" + postId + "/comments/" + commentDeleteResponse.getId())
                            .header(HttpHeaders.AUTHORIZATION,"Bearer a.b.c"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("INVALID_TOKEN"))
                    .andExpect(jsonPath("$.result.message").value("????????? ???????????????."))
                    .andDo(print());

            then(commentService).should(never()).delete(any(), any(), any());
        }

        @Test
        @DisplayName("?????? ?????? ??????(2) : Post?????? ??????")
        public void comment_delete_fail2() throws Exception {

            willThrow(new AppException(ErrorCode.POST_NOT_FOUND)).given(commentService).delete(any(), any(), any());

            mockMvc.perform(delete("/api/v1/posts/" + postId + "/comments/" + commentDeleteResponse.getId())
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("POST_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.message").value("?????? ???????????? ????????????."))
                    .andDo(print());

            then(commentService).should(times(1)).delete(any(), any(), any());
        }

        @Test
        @DisplayName("?????? ?????? ??????(3) : ????????? ?????????")
        public void comment_delete_fail3() throws Exception {

            willThrow(new AppException(ErrorCode.INVALID_PERMISSION)).given(commentService).delete(any(), any(), any());

            mockMvc.perform(delete("/api/v1/posts/" + postId + "/comments/" + commentDeleteResponse.getId())
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("INVALID_PERMISSION"))
                    .andExpect(jsonPath("$.result.message").value("???????????? ????????? ????????????."))
                    .andDo(print());

            then(commentService).should(times(1)).delete(any(), any(), any());
        }

        @Test
        @DisplayName("?????? ?????? ??????(4) : ?????????????????? ??????")
        public void comment_delete_fail4() throws Exception {

            willThrow(new AppException(ErrorCode.DATABASE_ERROR)).given(commentService).delete(any(), any(), any());

            mockMvc.perform(delete("/api/v1/posts/" + postId + "/comments/" + commentDeleteResponse.getId())
                            .header(HttpHeaders.AUTHORIZATION,"Bearer " + token))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("DATABASE_ERROR"))
                    .andExpect(jsonPath("$.result.message").value("DB??????"))
                    .andDo(print());

            then(commentService).should(times(1)).delete(any(), any(), any());
        }
    }
}