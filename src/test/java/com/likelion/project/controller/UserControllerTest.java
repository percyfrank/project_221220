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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    UserJoinRequest userJoinRequest = UserJoinRequest.builder().userName("user").password("password").build();
    UserJoinResponse userJoinResponse = UserJoinResponse.builder().userId(1).userName("user").build();
    UserLoginResponse userLoginResponse = new UserLoginResponse("jwt");

    @Test
    @DisplayName("회원가입 성공")
    @WithMockUser
    public void join_success() throws Exception {
        //given
        //when
        when(userService.join(any())).thenReturn(userJoinResponse);

        mockMvc.perform(post("/api/v1/users/join")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(userJoinRequest)))
                .andExpect(status().isOk())
                .andDo(print());

        //then
        verify(userService).join(any());
    }


    @Test
    @DisplayName("회원가입 실패 - 중복 유저")
    @WithMockUser
    public void join_fail_duplicated() throws Exception {
        //given
        //when
        when(userService.join(any()))
                .thenThrow(new AppException(ErrorCode.DUPLICATED_USER_NAME));

        mockMvc.perform(post("/api/v1/users/join")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(userJoinRequest)))
                .andDo(print())
                .andExpect(status().isConflict());

        //then
        verify(userService).join(any());
    }

    UserLoginRequest userLoginRequest = UserLoginRequest.builder()
            .userName("권오석")
            .password("1234")
            .build();

    @Test
    @DisplayName("로그인 성공")
    @WithMockUser
    public void login_success() throws Exception {
        //given
        //when
        when(userService.login(any(), any())).thenReturn(userLoginResponse);

        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(userLoginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").exists())
                .andExpect(jsonPath("$.result.jwt").exists());
        //then

        verify(userService).login(any(), any());
    }

    @Test
    @DisplayName("로그인 실패 - userName없음")
    @WithMockUser
    public void login_fail_empty_userName() throws Exception {
        //given
        //when
        when(userService.login(any(), any())).thenThrow(new AppException(ErrorCode.USERNAME_NOT_FOUND));

        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(userLoginRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());
        //then

        verify(userService).login(any(), any());
    }

    @Test
    @DisplayName("로그인 실패 - password틀림")
    @WithMockUser
    public void login_fail_password_wrong() throws Exception {
        //given
        //when
        when(userService.login(any(), any())).thenThrow(new AppException(ErrorCode.INVALID_PASSWORD));

        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(userLoginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        //then

        verify(userService).login(any(), any());
    }

}