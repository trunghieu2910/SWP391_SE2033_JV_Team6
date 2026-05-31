package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemLogRepository extends JpaRepository<SystemLog, Integer> {

}
