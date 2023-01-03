package com.likelion.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.project.domain.dto.user.*;
import com.likelion.project.exception.ErrorCode;
import com.likelion.project.exception.AppException;
import com.likelion.project.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserApiController.class)
class UserApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    UserJoinRequest userJoinRequest = new UserJoinRequest("user", "password");
    UserJoinResponse userJoinResponse = new UserJoinResponse(1, "user");
    UserLoginRequest userLoginRequest = new UserLoginRequest("권오석", "1234");
    UserLoginResponse userLoginResponse = new UserLoginResponse("jwt");

    @Test
    @DisplayName("회원가입 성공")
    @WithMockUser
    public void join_success() throws Exception {

        given(userService.join(any())).willReturn(userJoinResponse);

        mockMvc.perform(post("/api/v1/users/join")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(userJoinRequest)))
                .andExpect(status().isOk())
                .andDo(print());

        verify(userService,times(1)).join(any());
    }
    @Test
    @DisplayName("회원가입 실패 - 중복 유저")
    @WithMockUser
    public void join_fail_duplicated() throws Exception {

        given(userService.join(any()))
                .willThrow(new AppException(ErrorCode.DUPLICATED_USER_NAME));

        mockMvc.perform(post("/api/v1/users/join")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(userJoinRequest)))
                .andDo(print())
                .andExpect(status().isConflict());

        verify(userService,times(1)).join(any());
    }


    @Test
    @DisplayName("로그인 성공")
    @WithMockUser
    public void login_success() throws Exception {

        given(userService.login(any(), any())).willReturn(userLoginResponse);

        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(userLoginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").exists())
                .andExpect(jsonPath("$.result.jwt").exists());

        verify(userService,times(1)).login(any(), any());
    }

    @Test
    @DisplayName("로그인 실패 - userName없음")
    @WithMockUser
    public void login_fail_empty_userName() throws Exception {

        given(userService.login(any(), any())).willThrow(new AppException(ErrorCode.USERNAME_NOT_FOUND));

        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(userLoginRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService,times(1)).login(any(), any());
    }

    @Test
    @DisplayName("로그인 실패 - password틀림")
    @WithMockUser
    public void login_fail_password_wrong() throws Exception {

        given(userService.login(any(), any())).willThrow(new AppException(ErrorCode.INVALID_PASSWORD));

        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(userLoginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(userService,times(1)).login(any(), any());
    }

}