package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.User;
import com.mycompany.jpademo.backend.enums.RoleName;
import com.mycompany.jpademo.backend.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    boolean existsByUserName(String userName);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByNationalID(String nationalID);

    Page<User> findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String email, Pageable pageable);

    Page<User> findByRoleRoleNameAndStatus(RoleName roleName, UserStatus status, Pageable pageable);

    Page<User> findByRoleRoleName(RoleName roleName, Pageable pageable);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    Page<User> findAll(Pageable pageable);

    Long countByRoleRoleName(RoleName roleName);

    Long countByStatus(UserStatus status);

    Page<User> findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndRoleRoleName(
            String username, String email, RoleName roleName, Pageable pageable);

    Page<User> findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndStatus(
            String username, String email, UserStatus status, Pageable pageable);

    Page<User> findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndRoleRoleNameAndStatus(
            String username, String email, RoleName roleName, UserStatus status, Pageable pageable);

    @Query(value = """
    SELECT 
        FORMAT(createdAt, 'yyyy-MM') as month,
        COUNT(*) as count
    FROM Users 
    WHERE createdAt >= DATEADD(month, -6, GETDATE())
    GROUP BY FORMAT(createdAt, 'yyyy-MM')
    ORDER BY month ASC
    """, nativeQuery = true)
    List<Object[]> getUserRegistrationsByMonth();

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

    @Query(value = """
    SELECT 
        FORMAT(createdAt, 'yyyy-MM') as month,
        COUNT(*) as count
    FROM Users 
    WHERE (:startDate IS NULL OR createdAt >= :startDate)
      AND (:endDate IS NULL OR createdAt <= :endDate)
    GROUP BY FORMAT(createdAt, 'yyyy-MM')
    ORDER BY month ASC
    """, nativeQuery = true)
    List<Object[]> getUserRegistrationsByMonthWithFilter(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

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

    Optional<User> findFirstByRoleRoleName(RoleName roleName);

    Page<User> findByCreatedAtBetween(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<User> findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndCreatedAtBetween(
            String userName, String email, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<User> findByRoleRoleNameAndCreatedAtBetween(
            RoleName roleName, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<User> findByStatusAndCreatedAtBetween(
            UserStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<User> findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndRoleRoleNameAndCreatedAtBetween(
            String userName, String email, RoleName roleName, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<User> findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndStatusAndCreatedAtBetween(
            String userName, String email, UserStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<User> findByRoleRoleNameAndStatusAndCreatedAtBetween(
            RoleName roleName, UserStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<User> findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndRoleRoleNameAndStatusAndCreatedAtBetween(
            String userName, String email, RoleName roleName, UserStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
