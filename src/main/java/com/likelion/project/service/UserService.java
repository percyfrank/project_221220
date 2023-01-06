package com.likelion.project.service;

import com.likelion.project.domain.dto.user.*;
import com.likelion.project.jwt.JwtTokenUtil;
import com.likelion.project.domain.entity.User;
import com.likelion.project.exception.ErrorCode;
import com.likelion.project.exception.AppException;
import com.likelion.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @Value("${jwt.token.secret}")
    private String secretKey;

    private long expireTimeMs = 500 * 60 * 60; // 토근 유효기간 30분 설정

    public UserJoinResponse join(UserJoinRequest userJoinRequest) {

        // 중복 검사
        userRepository.findByUserName(userJoinRequest.getUserName())
                .ifPresent((user -> {
                    throw new AppException(ErrorCode.DUPLICATED_USER_NAME);
                }));

        // 회원 저장
        User savedUser = userRepository.save(
                User.createUser(userJoinRequest.getUserName(),
                        encoder.encode(userJoinRequest.getPassword())));

        return UserJoinResponse.of(savedUser);
    }

    public UserLoginResponse login(String userName, String password) {
        // userName 존재 확인
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));

        // password 일치 여부 확인
        if(!encoder.matches(password, user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        return new UserLoginResponse(JwtTokenUtil.createToken(userName, secretKey, expireTimeMs));
    }

    public UserRoleResponse changeRole(Integer id, UserRoleRequest userRoleRequest, String userName) {

        // 권한을 변경하고자 하는(@PathVariable의) 사용자가 있는지 확인
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));

        // 해당 요청을 수행하려는 사용자가 로그인된 사용자인지 확인
        User loginUser = userRepository.findByUserName(userName).orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));

        // 로그인된 사용자의 권한이 관리자 권한인지 확인
        if (!loginUser.getRole().equals("ROLE_ADMIN")) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 권한 변경이 되기를 바라는 요청의 권한 추출
        String requestedRole = userRoleRequest.getRole();

        // 현재 유저의 권한과 다른 권한으로 변경하고자 할때만 변경
        if (!user.getRole().equals(requestedRole)) {
            user.changeRole(requestedRole);
        }

        return new UserRoleResponse(id, requestedRole + " 권한으로 변경되었습니다.");

    }
}
