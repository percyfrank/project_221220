package com.likelion.project.service;

import com.likelion.project.domain.dto.alarm.AlarmResponse;
import com.likelion.project.domain.entity.Alarm;
import com.likelion.project.domain.entity.User;
import com.likelion.project.exception.AppException;
import com.likelion.project.exception.ErrorCode;
import com.likelion.project.repository.AlarmRepository;
import com.likelion.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AlarmResponse> getAlarmList(String userName, Pageable pageable) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND));

        Page<Alarm> alarmsList = alarmRepository.findAllByUserId(user.getId(), pageable);

        return alarmsList.map(AlarmResponse::of);
    }
}
