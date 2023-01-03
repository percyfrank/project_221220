package com.likelion.project.repository;

import com.likelion.project.domain.entity.Like;
import com.likelion.project.domain.entity.Post;
import com.likelion.project.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like,Integer> {

    Optional<Like> findByUserAndPost(User user, Post post);

    Long countByPost_Id(Integer postId);
}
