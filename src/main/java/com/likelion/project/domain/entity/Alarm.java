package com.likelion.project.domain.entity;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE alarm SET deleted_at = CURRENT_TIMESTAMP where id = ?")
public class Alarm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
//    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;              // 알람 받는 유저

    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;    // NEW_COMMENT_ON_POST, NEW_LIKE_ON_POST

    private Integer fromUserId;     // 알람(댓글,좋아요) 발생시킨 유저
    private Integer targetId;       // 알람 발생된 게시글(댓글, 좋아요가 달린)

//    private String text;            // 알람 타입에 맞게  new comment!, new like! 입력

    public static Alarm createAlarm(User user,Post post,AlarmType alarmType) {
        return Alarm.builder()
                .user(post.getUser())
                .alarmType(alarmType)
                .fromUserId(user.getId())
                .targetId(post.getId())
                .build();
    }
}