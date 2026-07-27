package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.RoleName;
import com.mycompany.jpademo.backend.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    // Tìm bằng userName (field trong TS-49)
    Optional<User> findByUserName(String userName);

    // Tìm bằng email (dùng cho ForgotPassword)
    Optional<User> findByEmail(String email);

    // Tìm bằng nhiều loại login (username / email / phone / nationalID)
    @Query("SELECT u FROM User u WHERE u.userName = :login OR u.email = :email OR u.phoneNumber = :phone OR u.nationalID = :nationalId")
    Optional<User> findByEmailOrUsernameOrPhoneNumberOrNationalId(
            @Param("login") String login,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("nationalId") String nationalId
    );

    Page<User> findByRoleRoleNameAndStatus(RoleName roleName, UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR " +
            "  LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "  LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:role IS NULL OR u.role.roleName = :role) " +
            "AND (:status IS NULL OR u.status = :status) " +
            "AND (:startDate IS NULL OR :endDate IS NULL OR u.createdAt BETWEEN :startDate AND :endDate)")
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("role") RoleName role,
            @Param("status") UserStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    boolean existsByUserName(String userName);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByNationalID(String nationalID);

    @Query("SELECT u FROM User u " +
            "WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    //Giang
    @Query("SELECT COUNT(u) FROM User u WHERE " +
            "(CAST(:startDate AS timestamp) IS NULL OR u.createdAt >= :startDate) AND " +
            "(CAST(:endDate AS timestamp) IS NULL OR u.createdAt <= :endDate)")
    long countUsersWithDateFilter(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role.roleName = :roleName AND " +
            "(CAST(:startDate AS timestamp) IS NULL OR u.createdAt >= :startDate) AND " +
            "(CAST(:endDate AS timestamp) IS NULL OR u.createdAt <= :endDate)")
    long countUsersByRoleWithDateFilter(@Param("roleName") RoleName roleName, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status AND " +
            "(CAST(:startDate AS timestamp) IS NULL OR u.createdAt >= :startDate) AND " +
            "(CAST(:endDate AS timestamp) IS NULL OR u.createdAt <= :endDate)")
    long countUsersByStatusWithDateFilter(@Param("status") UserStatus status, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLogoutTime = :logoutTime WHERE u.userId = :userId")
    void updateLastLogoutTime(@Param("userId") Integer userId, @Param("logoutTime") LocalDateTime logoutTime);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchUsersByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT FUNCTION('FORMAT', u.createdAt, 'MM/yyyy') as month, COUNT(u) as count " +
            "FROM User u " +
            "WHERE (:start IS NULL OR u.createdAt >= :start) " +
            "AND (:end IS NULL OR u.createdAt <= :end) " +
            "GROUP BY FUNCTION('FORMAT', u.createdAt, 'MM/yyyy') " +
            "ORDER BY month ASC")
    List<Object[]> getMonthlyUserRegistrations(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);
}
