package com.likelion.project.domain.entity;

import com.likelion.project.domain.dto.comment.CommentResponse;
import lombok.*;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public CommentResponse updateComment(String comment) {
        this.comment = comment;
        return new CommentResponse(this.id, this.comment, this.user.getUserName(), this.post.getId(),
                this.getUpdatedAt());
    }
}
