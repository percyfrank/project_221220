package com.likelion.project.controller;

import com.likelion.project.domain.dto.alarm.AlarmResponse;
import com.likelion.project.exception.Response;
import com.likelion.project.service.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alarms")
@RequiredArgsConstructor
@Slf4j
public class AlarmApiController {

    private final AlarmService alarmService;

    // 특정 유저의 알람 리스트 조회
    @GetMapping("")
    public Response<Page<AlarmResponse>> alarmList(Authentication authentication,
             @PageableDefault(size = 20, sort = {"registeredAt"}, direction = Sort.Direction.DESC)
             Pageable pageable) {
        log.info("알림 리스트 조회 성공");
        return Response.success(alarmService.getAlarmList(authentication.getName(), pageable));
    }
}