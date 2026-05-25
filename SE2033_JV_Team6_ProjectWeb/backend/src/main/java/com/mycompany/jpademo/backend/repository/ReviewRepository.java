package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    // Hàm tìm lời phê kết luận của bác sĩ dựa theo mã ca khám (SessionID)
    Optional<Review> findBySessionSessionID(Integer sessionID);
}