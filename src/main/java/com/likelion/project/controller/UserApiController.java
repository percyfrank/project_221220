package com.likelion.project.controller;


import com.likelion.project.domain.dto.user.*;
import com.likelion.project.exception.Response;
import com.likelion.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "1. 회원")
public class UserApiController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "유저이름, 패스워드로 가입")
    @PostMapping("/join")
    public Response<UserJoinResponse> join(@RequestBody UserJoinRequest userJoinRequest) {
        log.info("회원가입 성공");
        return Response.success(userService.join(userJoinRequest));
    }
    @Operation(summary = "로그인", description = "유저이름, 패스워드로 회원 존재하면 jwt 토큰 반환과 함께 로그인")
    @PostMapping("/login")
    public Response<UserLoginResponse> login(@RequestBody UserLoginRequest userLoginRequest) {
        log.info("로그인 성공");
        return Response.success(userService.login(userLoginRequest.getUserName(), userLoginRequest.getPassword()));
    }

    @PostMapping("/{userId}/role/change")
    public Response<UserRoleChangeResponse> roleChange(@PathVariable("userId") Integer id, @RequestBody UserRoleChangeRequest userRoleChangeRequest, Authentication authentication) {
        UserRoleChangeResponse response = userService.changeRole(id, userRoleChangeRequest, authentication.getName());
        log.info("유저 권한 변경 성공");
        return Response.success(response);
    }

}

