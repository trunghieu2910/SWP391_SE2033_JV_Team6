package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.BlockedIP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockedIPRepository extends JpaRepository<BlockedIP, String> {
    boolean existsByIpAddress(String ipAddress);
}
