package com.likelion.project.service;

import com.likelion.project.domain.dto.user.UserDto;
import com.likelion.project.domain.dto.user.UserJoinRequest;
import com.likelion.project.domain.entity.User;
import com.likelion.project.exception.ErrorCode;
import com.likelion.project.exception.UserException;
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

    public UserDto join(UserJoinRequest userJoinRequest) {

        // 중복 검사
        userRepository.findByUserName(userJoinRequest.getUserName())
                .ifPresent((user -> {
                    throw new UserException(ErrorCode.DUPLICATED_USER_NAME);
                }));

        // 회원 저장
        User savedUser = userRepository.save(
                userJoinRequest.toEntity(encoder.encode(userJoinRequest.getPassword())));

        return UserDto.builder()
                .id(savedUser.getId())
                .userName(savedUser.getUserName())
                .build();

    }
}
