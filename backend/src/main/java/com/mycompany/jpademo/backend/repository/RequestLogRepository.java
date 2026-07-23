package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.dto.response.EndpointRequestStats;
import com.mycompany.jpademo.backend.dto.response.IpRequestStats;
import com.mycompany.jpademo.backend.entity.RequestLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {
    @Query("SELECT new com.mycompany.jpademo.backend.dto.response.IpRequestStats(r.ipAddress, COUNT(r)) " +
            "FROM RequestLog r " +
            "GROUP BY r.ipAddress " +
            "ORDER BY COUNT(r) DESC")
    List<IpRequestStats> findTopIps(Pageable pageable);

    @Query("SELECT new com.mycompany.jpademo.backend.dto.response.EndpointRequestStats(r.uri, r.method, COUNT(r)) " +
            "FROM RequestLog r " +
            "GROUP BY r.uri, r.method " +
            "ORDER BY COUNT(r) DESC")
    List<EndpointRequestStats> findTopEndpoints(Pageable pageable);

    @Query("SELECT new com.mycompany.jpademo.backend.dto.response.IpRequestStats(r.ipAddress, COUNT(r)) " +
            "FROM RequestLog r " +
            "WHERE (:startDate IS NULL OR r.timestamp >= :startDate) " +
            "AND (:endDate IS NULL OR r.timestamp <= :endDate) " +
            "GROUP BY r.ipAddress " +
            "ORDER BY COUNT(r) DESC")
    List<IpRequestStats> findTopIpsWithDateFilter(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate,
                                                  Pageable pageable);

    @Query("SELECT new com.mycompany.jpademo.backend.dto.response.EndpointRequestStats(r.uri, r.method, COUNT(r)) " +
            "FROM RequestLog r " +
            "WHERE (:startDate IS NULL OR r.timestamp >= :startDate) " +
            "AND (:endDate IS NULL OR r.timestamp <= :endDate) " +
            "GROUP BY r.uri, r.method " +
            "ORDER BY COUNT(r) DESC")
    List<EndpointRequestStats> findTopEndpointsWithDateFilter(@Param("startDate") LocalDateTime startDate,
                                                              @Param("endDate") LocalDateTime endDate,
                                                              Pageable pageable);

    @Query("SELECT COUNT(r) FROM RequestLog r " +
            "WHERE (:startDate IS NULL OR r.timestamp >= :startDate) " +
            "AND (:endDate IS NULL OR r.timestamp <= :endDate)")
    long countWithDateFilter(@Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(r) * 1.0 / " +
            "CASE WHEN (:endDate IS NULL OR :startDate IS NULL) THEN 1 " +
            "ELSE (FUNCTION('DATEDIFF', MINUTE, :startDate, :endDate)) END " +
            "FROM RequestLog r " +
            "WHERE (:startDate IS NULL OR r.timestamp >= :startDate) " +
            "AND (:endDate IS NULL OR r.timestamp <= :endDate)")
    Double getAvgRequestsPerMinute(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT FUNCTION('FORMAT', r.timestamp, 'MM/yyyy') as month, COUNT(r) as count " +
            "FROM RequestLog r " +
            "WHERE (:start IS NULL OR r.timestamp >= :start) " +
            "AND (:end IS NULL OR r.timestamp <= :end) " +
            "GROUP BY FUNCTION('FORMAT', r.timestamp, 'MM/yyyy') " +
            "ORDER BY month ASC")
    List<Object[]> getMonthlyRequestTrend(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);
}