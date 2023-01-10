package com.likelion.project.controller;

import com.likelion.project.domain.dto.alarm.AlarmResponse;
import com.likelion.project.exception.Response;
import com.likelion.project.service.AlarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    @Tag(name = "6. 알람")
    @Operation(summary = "알람 리스트 조회", description = "권한 필요 & 최근 생성순으로 20개씩 페이징")
    @GetMapping("")
    public Response<Page<AlarmResponse>> alarmList(@Parameter(hidden = true) Authentication authentication,
             @PageableDefault(size = 20, sort = {"registeredAt"}, direction = Sort.Direction.DESC)
             Pageable pageable) {
        log.info("알림 리스트 조회 성공");
        return Response.success(alarmService.getAlarmList(authentication.getName(), pageable));
    }
}