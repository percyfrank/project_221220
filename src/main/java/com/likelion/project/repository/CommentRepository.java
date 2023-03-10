package com.likelion.project.repository;

import com.likelion.project.domain.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment,Integer> {

    Page<Comment> findByPostId(Integer postId, Pageable pageable);
}
