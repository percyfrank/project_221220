package com.likelion.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.project.annotation.WebMvcTestSecurity;
import com.likelion.project.domain.dto.alarm.AlarmResponse;
import com.likelion.project.domain.dto.comment.CommentResponse;
import com.likelion.project.domain.entity.AlarmType;
import com.likelion.project.jwt.JwtTokenUtil;
import com.likelion.project.service.AlarmService;
import com.likelion.project.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTestSecurity(value = AlarmApiController.class)
@WebAppConfiguration
class AlarmApiControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    AlarmService alarmService;
    @MockBean
    CommentService commentService;
    @MockBean
    JwtTokenUtil jwtTokenUtil;
    @Value("${jwt.token.secret}") String secretKey;
    private final AlarmResponse alarmResponse =
            new AlarmResponse(1, AlarmType.NEW_COMMENT_ON_POST, 1, 1, "new comment!", LocalDateTime.now());
    private final AlarmResponse alarmResponse2 =
            new AlarmResponse(2, AlarmType.NEW_LIKE_ON_POST, 1, 1, "new like!", LocalDateTime.now());


    @Nested
    @DisplayName("조회")
    class CommentList {

        @Test
        @DisplayName("알람 목록 조회 성공")
        public void alarm_list_success() throws Exception {

            String token = jwtTokenUtil.createToken("user", secretKey, 1000 * 60 * 60L);

            PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"registeredAt");
            PageImpl<AlarmResponse> alarmPage = new PageImpl<>(List.of(alarmResponse,alarmResponse2));

            given(alarmService.getAlarmList("user", pageable)).willReturn(alarmPage);

            mockMvc.perform(get("/api/v1/alarms")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.content").isArray())
                    .andExpect(jsonPath("$['result']['content'][0]").exists())
                    .andExpect(jsonPath("$['result']['content'][1]").exists())
                    .andExpect(jsonPath("$.result.pageable").exists())
                    .andExpect(jsonPath("$.result.size").value(2))
                    .andExpect(jsonPath("$.result.sort").exists())
                    .andDo(print());

            then(alarmService).should(times(1)).getAlarmList("user",pageable);
        }

        @Test
        @DisplayName("알람 목록 조회 실패 - 로그인하지 않은 경우")
        public void alarm_list_fail() throws Exception {

            mockMvc.perform(get("/api/v1/alarms"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.resultCode").value("ERROR"))
                    .andExpect(jsonPath("$.result.errorCode").value("TOKEN_NOT_FOUND"))
                    .andExpect(jsonPath("$.result.message").value("토큰이 존재하지 않습니다."))
                    .andDo(print());

            then(alarmService).should(never()).getAlarmList(any(), any());
        }
    }
}