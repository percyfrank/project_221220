package com.likelion.project.controller;


import com.likelion.project.domain.dto.user.*;
import com.likelion.project.exception.Response;
import com.likelion.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/join")
    public Response<UserJoinResponse> join(@RequestBody UserJoinRequest userJoinRequest) {
        log.info("회원가입 성공");
        return Response.success(userService.join(userJoinRequest));
    }

    @PostMapping("/login")
    public Response<UserLoginResponse> login(@RequestBody UserLoginRequest userLoginRequest) {
        log.info("로그인 성공");
        return Response.success(userService.login(userLoginRequest.getUserName(), userLoginRequest.getPassword()));
    }


}

