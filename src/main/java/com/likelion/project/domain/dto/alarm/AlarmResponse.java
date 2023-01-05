package com.likelion.project.domain.dto.alarm;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.likelion.project.domain.entity.Alarm;
import com.likelion.project.domain.entity.AlarmType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class AlarmResponse {
    private Integer id;
    private AlarmType alarmType;
    private Integer fromUserId; //알림을 발생시킨 user
    private Integer targetId;   //알림이 발생된 post
    private String text;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd a HH:mm:ss")
    private LocalDateTime createdAt;

    public static AlarmResponse of(Alarm alarm) {
        return AlarmResponse.builder()
                .id(alarm.getId())
                .alarmType(alarm.getAlarmType())
                .fromUserId(alarm.getFromUserId())
                .targetId(alarm.getTargetId())
                .text(alarm.getText())
                .createdAt(alarm.getRegisteredAt())
                .build();
    }
}
