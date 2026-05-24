package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemLogRepository extends JpaRepository<SystemLog, Integer> {
    List<SystemLog> findByUserUserId(Integer userId);

    List<SystemLog> findByAction(String action);

    List<SystemLog> findByDescriptionContaining(String keyword);

    List<SystemLog> findByTargetTypeContaining(String targetType);

    List<SystemLog> findAllByOrderByPerformedAtDesc();
}
