package com.likelion.project.controller;


import com.likelion.project.domain.dto.user.UserDto;
import com.likelion.project.domain.dto.user.UserJoinRequest;
import com.likelion.project.domain.dto.user.UserJoinResponse;
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
        UserDto userDto = userService.join(userJoinRequest);
        log.info("회원가입 성공");
        return Response.success(new UserJoinResponse(userDto.getId(), userDto.getUserName()));
    }


}

