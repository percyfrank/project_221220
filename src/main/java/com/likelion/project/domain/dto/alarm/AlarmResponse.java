package com.likelion.project.domain.dto.alarm;

import com.likelion.project.domain.entity.Alarm;
import com.likelion.project.domain.entity.AlarmType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.text.SimpleDateFormat;

@Getter
@AllArgsConstructor
@Builder
public class AlarmResponse {
    private Integer id;
    private AlarmType alarmType;
    private Integer fromUserId; //알림을 발생시킨 user
    private Integer targetId;   //알림이 발생된 post
    private String text;
    private String createdAt;

    public static AlarmResponse of(Alarm alarm) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd a HH:mm:ss");
        return AlarmResponse.builder().
                id(alarm.getId())
                .alarmType(alarm.getAlarmType())
                .fromUserId(alarm.getFromUserId())
                .targetId(alarm.getTargetId())
                .text(alarm.getText())
                .createdAt(simpleDateFormat.format(alarm.getRegisteredAt()))
                .build();
    }
}
