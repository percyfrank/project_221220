package com.likelion.project.domain.entity;

import com.likelion.project.exception.AppException;
import com.likelion.project.exception.ErrorCode;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
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
@SQLDelete(sql = "UPDATE user SET deleted_at = CURRENT_TIMESTAMP where id = ?")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String password;
    private String userName;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    public void changeRole(String requestedRole) {
        if(requestedRole.equals("ADMIN")) {
            this.role = UserRole.ROLE_ADMIN;
        } else if (requestedRole.equals("USER")) {
            this.role = UserRole.ROLE_USER;
        } else {
            throw new AppException(ErrorCode.INVALID_VALUE);
        }
    }
}

