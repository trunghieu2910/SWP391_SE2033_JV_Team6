package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.dto.response.EndpointRequestStats;
import com.mycompany.jpademo.backend.dto.response.IpRequestStats;
import com.mycompany.jpademo.backend.entity.RequestLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.repository.query.Param;

@Repository
public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {
    @Query("SELECT new com.mycompany.jpademo.backend.dto.response.IpRequestStats(r.ipAddress, COUNT(r)) " +
            "FROM RequestLog r GROUP BY r.ipAddress ORDER BY COUNT(r) DESC")
    List<IpRequestStats> findTopIps(Pageable pageable);

    @Query("SELECT new com.mycompany.jpademo.backend.dto.response.EndpointRequestStats(r.uri, r.method, COUNT(r)) " +
            "FROM RequestLog r GROUP BY r.uri, r.method ORDER BY COUNT(r) DESC")
    List<EndpointRequestStats> findTopEndpoints(Pageable pageable);

    long countByTimestampAfter(LocalDateTime time);

    @Query("SELECT new com.mycompany.jpademo.backend.dto.response.IpRequestStats(r.ipAddress, COUNT(r)) " +
           "FROM RequestLog r WHERE (CAST(:startDate AS timestamp) IS NULL OR r.timestamp >= :startDate) AND (CAST(:endDate AS timestamp) IS NULL OR r.timestamp <= :endDate) GROUP BY r.ipAddress ORDER BY COUNT(r) DESC")
    List<IpRequestStats> findTopIpsWithDateFilter(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT new com.mycompany.jpademo.backend.dto.response.EndpointRequestStats(r.uri, r.method, COUNT(r)) " +
           "FROM RequestLog r WHERE (CAST(:startDate AS timestamp) IS NULL OR r.timestamp >= :startDate) AND (CAST(:endDate AS timestamp) IS NULL OR r.timestamp <= :endDate) GROUP BY r.uri, r.method ORDER BY COUNT(r) DESC")
    List<EndpointRequestStats> findTopEndpointsWithDateFilter(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT COUNT(r) FROM RequestLog r WHERE (CAST(:startDate AS timestamp) IS NULL OR r.timestamp >= :startDate) AND (CAST(:endDate AS timestamp) IS NULL OR r.timestamp <= :endDate)")
    long countWithDateFilter(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
