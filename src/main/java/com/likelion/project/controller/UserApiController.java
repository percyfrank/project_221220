package com.likelion.project.controller;


import com.likelion.project.domain.dto.user.*;
import com.likelion.project.exception.Response;
import com.likelion.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserApiController {

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

    @PostMapping("/{userId}/role/change")
    public Response<UserRoleResponse> changerole(@PathVariable("userId") Integer id,
                                                 @RequestBody UserRoleRequest userRoleRequest,
                                                 Authentication authentication) {
        String userName = authentication.getName();
        UserRoleResponse response = userService.changerole(id, userRoleRequest, userName);
        log.info("유저 권한 변경 성공");
        return Response.success(response);
    }

}

