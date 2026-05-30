package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemLogRepository extends JpaRepository<SystemLog, Integer> {
    Page<SystemLog> findByUserUserId(Integer userId, Pageable pageable);

    Page<SystemLog> findByAction(String action, Pageable pageable);

    Page<SystemLog> findByDescriptionContainingIgnoreCase(String keyword, Pageable pageable);

    Page<SystemLog> findByUserUserIdAndAction(
            Integer userId, String action, Pageable pageable);

    Page<SystemLog> findByUserUserIdAndDescriptionContainingIgnoreCase(
            Integer userId, String keyword, Pageable pageable);

    Page<SystemLog> findByActionAndDescriptionContainingIgnoreCase(
            String action, String keyword, Pageable pageable);

    Page<SystemLog> findByUserUserIdAndActionAndDescriptionContainingIgnoreCase(
            Integer userId, String action, String keyword, Pageable pageable);

    List<SystemLog> findTop10ByUser_UserIdOrderByPerformedAtDesc(Integer userId);
}
