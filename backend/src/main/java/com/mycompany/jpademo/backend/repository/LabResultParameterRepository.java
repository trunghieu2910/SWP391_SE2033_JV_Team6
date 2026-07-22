package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.LabResultParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Data access for individual measured parameter values belonging
 * to a completed LabResult.
 */
@Repository
public interface LabResultParameterRepository extends JpaRepository<LabResultParameter, Integer> {

    /** Fetches all recorded parameter values for a given lab result. */
    List<LabResultParameter> findByLabResultLabResultId(Integer labResultId);
}