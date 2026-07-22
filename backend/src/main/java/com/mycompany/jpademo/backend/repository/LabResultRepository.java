package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.LabResult;
import com.mycompany.jpademo.backend.enums.LabResultStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access for LabResult orders.
 */
@Repository
public interface LabResultRepository extends JpaRepository<LabResult, Integer> {

    /** Fetches all lab result orders (without their parameters) for a given diagnosis session. */
    List<LabResult> findByDiagnosisSessionSessionId(Integer sessionId);

    @Query("SELECT lr FROM LabResult lr " +
            "LEFT JOIN FETCH lr.labResultParameters lrp " +
            "LEFT JOIN FETCH lrp.parameter p " +
            "WHERE lr.diagnosisSession.sessionId = :sessionId")

    /**
     * Fetches all lab results for a session together with their
     * parameters and parameter catalog entries eagerly loaded
     * (avoids N+1 queries when rendering the full lab result list).
     */
    List<LabResult> findBySessionIdWithParameters(@Param("sessionId") Integer sessionId);

    /**
     * Finds a LabResult by id, constrained to a specific status.
     * Used by the LIS integration flow to guarantee results are only
     * ever applied to an order that is still PENDING (idempotency guard).
     */
    Optional<LabResult> findByLabResultIdAndStatus(Integer labResultId, LabResultStatus status);

    /** Used to prevent creating two lab orders of the same test type for the same session. */
    boolean existsByDiagnosisSession_SessionIdAndTestType(Integer sessionId, String testType);
}