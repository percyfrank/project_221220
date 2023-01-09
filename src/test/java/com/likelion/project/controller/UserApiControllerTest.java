package com.likelion.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.project.annotation.WebMvcTestSecurity;
import com.likelion.project.domain.dto.user.*;
import com.likelion.project.exception.ErrorCode;
import com.likelion.project.exception.AppException;
import com.likelion.project.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTestSecurity(value = UserApiController.class)
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
    public void join_success() throws Exception {

        given(userService.join(any())).willReturn(userJoinResponse);

        mockMvc.perform(post("/api/v1/users/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(userJoinRequest)))
                .andExpect(status().isOk())
                .andDo(print());

        then(userService).should(times(1)).join(any());
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 유저")
    public void join_fail_duplicated() throws Exception {

        given(userService.join(any()))
                .willThrow(new AppException(ErrorCode.DUPLICATED_USER_NAME));

        mockMvc.perform(post("/api/v1/users/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(userJoinRequest)))
                .andDo(print())
                .andExpect(status().isConflict());

        then(userService).should(times(1)).join(any());
    }

    @Test
    @DisplayName("로그인 성공")
    public void login_success() throws Exception {

        given(userService.login(any(), any())).willReturn(userLoginResponse);

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(userLoginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").exists())
                .andExpect(jsonPath("$.result.jwt").exists());

        then(userService).should(times(1)).login(any(), any());
    }

    @Test
    @DisplayName("로그인 실패 - userName없음")
    public void login_fail_empty_userName() throws Exception {

        given(userService.login(any(), any())).willThrow(new AppException(ErrorCode.USERNAME_NOT_FOUND));

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(userLoginRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());

        then(userService).should(times(1)).login(any(), any());
    }

    @Test
    @DisplayName("로그인 실패 - password틀림")
    public void login_fail_password_wrong() throws Exception {

        given(userService.login(any(), any())).willThrow(new AppException(ErrorCode.INVALID_PASSWORD));

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(userLoginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        then(userService).should(times(1)).login(any(), any());
    }
}