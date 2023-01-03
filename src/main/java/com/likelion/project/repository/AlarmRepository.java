package com.likelion.project.repository;

import com.likelion.project.domain.entity.Alarm;
import com.likelion.project.domain.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm,Integer> {

    Page<Alarm> findAllByUserId(Integer userId, Pageable pageable);
}