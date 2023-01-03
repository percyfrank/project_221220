package com.likelion.project.repository;

import com.likelion.project.domain.entity.Post;
import com.likelion.project.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface PostRepository extends JpaRepository<Post, Integer> {

    Page<Post> findAllByUser(User user, Pageable pageable);
}
