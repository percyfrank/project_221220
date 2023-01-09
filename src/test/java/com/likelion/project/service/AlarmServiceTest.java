package com.likelion.project.service;

import com.likelion.project.domain.dto.alarm.AlarmResponse;
import com.likelion.project.domain.entity.*;
import com.likelion.project.exception.AppException;
import com.likelion.project.exception.ErrorCode;
import com.likelion.project.repository.AlarmRepository;
import com.likelion.project.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AlarmServiceTest {

    @Mock
    private AlarmRepository alarmRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private AlarmService alarmService;
    private User user;

    @BeforeEach
    void setup() {
        user = User.builder().id(1).userName("user").password("password").build();
    }

    @Test
    @DisplayName("알람 리스트 조회 성공")
    public void alarmList_success() {

        Alarm alarm1 = Alarm.builder().id(1).user(user).alarmType(AlarmType.NEW_COMMENT_ON_POST).fromUserId(1).targetId(1).build();
        Alarm alarm2 = Alarm.builder().id(2).user(user).alarmType(AlarmType.NEW_COMMENT_ON_POST).fromUserId(1).targetId(1).build();
        PageImpl<Alarm> alarmList = new PageImpl<>(List.of(alarm1, alarm2));
        PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"registeredAt");

        given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.of(user));
        given(alarmRepository.findAllByUserId(user.getId(), pageable)).willReturn(alarmList);

        Page<AlarmResponse> responsePage = alarmService.getAlarmList(user.getUserName(), pageable);

        assertThat(responsePage.getTotalPages()).isEqualTo(1);
        assertThat(responsePage.getTotalElements()).isEqualTo(2);

        then(userRepository).should(times(1)).findByUserName(user.getUserName());
        then(alarmRepository).should(times(1)).findAllByUserId(user.getId(), pageable);
    }

    @Test
    @DisplayName("알람 리스트 조회 실패 - 유저 없음")
    public void alarmList_fail() {

        PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"registeredAt");
        given(userRepository.findByUserName(user.getUserName())).willReturn(Optional.empty());

        AppException appException = assertThrows(AppException.class, () -> alarmService.getAlarmList(user.getUserName(), pageable));

        assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.USERNAME_NOT_FOUND);
        assertThat(appException.getErrorCode().getMessage()).isEqualTo("Not founded");

        then(userRepository).should(times(1)).findByUserName(user.getUserName());
    }
}