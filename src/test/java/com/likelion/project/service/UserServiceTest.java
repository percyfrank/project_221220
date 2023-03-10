package com.likelion.project.service;

import com.likelion.project.domain.dto.user.*;
import com.likelion.project.domain.entity.User;
import com.likelion.project.domain.entity.UserRole;
import com.likelion.project.exception.AppException;
import com.likelion.project.exception.ErrorCode;
import com.likelion.project.jwt.JwtTokenUtil;
import com.likelion.project.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder encoder;
    @InjectMocks
    private UserService userService;
    @Value("${jwt.token.secret}") String secretKey;
    private User user;
    private User adminUser;
    private User mockUser;
    private User mockAdminUser;

    @BeforeEach
    void setup() {
        user = User.builder().id(1).userName("user").password("password").role(UserRole.ROLE_USER).build();
        adminUser = User.builder().id(2).userName("admin").password("password").role(UserRole.ROLE_ADMIN).build();
        mockUser = mock(User.class);
        mockAdminUser = mock(User.class);
    }

    @Nested
    @DisplayName("회원가입")
    class userJoin {

        UserJoinRequest request = new UserJoinRequest("user", "password");

        @Test
        @DisplayName("회원가입 성공")
        public void join_success() {

            given(userRepository.findByUserName(request.getUserName())).willReturn(Optional.empty());
            given(encoder.encode(request.getPassword())).willReturn(user.getPassword());
            given(userRepository.save(any(User.class))).willReturn(user);

            UserJoinResponse response = userService.join(request);
            assertThat(response.getUserId()).isEqualTo(user.getId());
            assertThat(response.getUserName()).isEqualTo(user.getUserName());

            then(userRepository).should(times(1)).findByUserName(request.getUserName());
            then(encoder).should(times(1)).encode(request.getPassword());
            then(userRepository).should(times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("회원가입 실패 - 중복 유저")
        public void join_fail1() {

            given(userRepository.findByUserName(request.getUserName())).willReturn(Optional.of(user));

            AppException appException = assertThrows(AppException.class, () -> userService.join(request));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_USER_NAME);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("UserName이 중복됩니다.");

            then(userRepository).should(times(1)).findByUserName(request.getUserName());
        }
    }

    @Nested
    @DisplayName("로그인")
    class userLogin {

        UserLoginRequest request = new UserLoginRequest("user", "password");

        @Test
        @DisplayName("로그인 성공")
        public void login_success() {

            MockedStatic<JwtTokenUtil> jwtTokenUtilMockedStatic = mockStatic(JwtTokenUtil.class);

            given(userRepository.findByUserName(request.getUserName())).willReturn(Optional.of(user));
            given(encoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
            given(JwtTokenUtil.createToken(request.getUserName(), secretKey, 500 * 60 * 60)).willReturn("token");

            UserLoginResponse response = userService.login(request.getUserName(), request.getPassword());

            assertThat(response.getJwt()).isEqualTo("token");

            jwtTokenUtilMockedStatic.close();
        }

        @Test
        @DisplayName("로그인 실패 - 유저 없음")
        public void login_fail1() {

            given(userRepository.findByUserName(request.getUserName())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class, () -> userService.login(request.getUserName(), request.getPassword()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.USERNAME_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("Not founded");

            then(userRepository).should(times(1)).findByUserName(request.getUserName());
        }

        @Test
        @DisplayName("로그인 실패 - 패스워드 불일치")
        public void login_fail2() {

            given(userRepository.findByUserName(request.getUserName())).willReturn(Optional.of(user));
            given(encoder.matches(request.getPassword(), user.getPassword())).willReturn(false);

            AppException appException = assertThrows(AppException.class, () -> userService.login(request.getUserName(), request.getPassword()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PASSWORD);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("패스워드가 잘못되었습니다.");

            then(userRepository).should(times(1)).findByUserName(request.getUserName());
            then(encoder).should(times(1)).matches(request.getPassword(), user.getPassword());
        }

    }

    @Nested
    @DisplayName("권한 변경")
    class changeRole {

        final UserRoleChangeRequest request = new UserRoleChangeRequest("ROLE_ADMIN");

        @Test
        @DisplayName("권한 변경 성공")
        public void changeRole_success() {

            given(userRepository.findByUserName(adminUser.getUserName())).willReturn(Optional.of(adminUser));
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
//            given(mockAdminUser.getRole()).willReturn(adminUser.getRole());

//            UserRoleChangeResponse response = userService.changeRole(user.getId(), request, adminUser.getUserName());
            UserRoleChangeResponse response = assertDoesNotThrow(() -> userService.changeRole(user.getId(), request, adminUser.getUserName()));

            assertThat(response.getMessage()).isEqualTo("ROLE_ADMIN 권한으로 변경되었습니다.");
            assertThat(response.getUserId()).isEqualTo(user.getId());

            then(userRepository).should(times(1)).findById(user.getId());
            then(userRepository).should(times(1)).findByUserName(adminUser.getUserName());
        }

        @Test
        @DisplayName("권한 변경 실패 - 로그인된 사용자인지 확인")
        public void changeRole_fail1() {

            given(userRepository.findByUserName(adminUser.getUserName())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> userService.changeRole(user.getId(), request, adminUser.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.USERNAME_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("Not founded");

            then(userRepository).should(times(1)).findByUserName(adminUser.getUserName());
        }

        @Test
        @DisplayName("권한 변경 실패 - 유저 없음")
        public void changeRole_fail2() {

            given(userRepository.findByUserName(adminUser.getUserName())).willReturn(Optional.of(adminUser));
            given(userRepository.findById(user.getId())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> userService.changeRole(user.getId(), request, adminUser.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.USERNAME_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("Not founded");

            then(userRepository).should(times(1)).findByUserName(adminUser.getUserName());
            then(userRepository).should(times(1)).findById(user.getId());
        }

        @Test
        @DisplayName("권한 변경 실패 - 관리자 권한이 아님")
        public void changeRole_fail3() {
            given(userRepository.findByUserName(adminUser.getUserName())).willReturn(Optional.of(mockAdminUser));
            given(userRepository.findById(user.getId())).willReturn(Optional.of(mockUser));
            given(mockAdminUser.getRole()).willReturn(user.getRole());

            AppException appException = assertThrows(AppException.class,
                    () -> userService.changeRole(user.getId(), request, adminUser.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("사용자가 권한이 없습니다.");

            then(userRepository).should(times(1)).findByUserName(adminUser.getUserName());
            then(userRepository).should(times(1)).findById(user.getId());
        }

        @Test
        @DisplayName("권한 변경 실패 - 권한 변경 요청이 이상함")
        public void changeRole_fail4() {

            given(userRepository.findByUserName(adminUser.getUserName())).willReturn(Optional.of(mockAdminUser));
            given(userRepository.findById(user.getId())).willReturn(Optional.of(mockUser));
            given(mockAdminUser.getRole()).willReturn(adminUser.getRole());

            AppException appException = assertThrows(AppException.class,
                    () -> userService.changeRole(user.getId(), request, adminUser.getUserName()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("사용자가 권한이 없습니다.");

            then(userRepository).should(times(1)).findByUserName(adminUser.getUserName());
            then(userRepository).should(times(1)).findById(user.getId());
        }
    }
}