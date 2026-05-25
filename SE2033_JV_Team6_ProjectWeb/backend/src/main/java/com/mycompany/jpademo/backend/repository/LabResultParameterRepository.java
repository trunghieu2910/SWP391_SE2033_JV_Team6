package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.LabResultParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabResultParameterRepository extends JpaRepository<LabResultParameter, Integer> {
    List<LabResultParameter> findByLabResultLabResultID(Integer labResultID);
}