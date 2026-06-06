package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.SymptomDetails;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@Transactional
public interface SymptomDetailsRepository extends JpaRepository<SymptomDetails, Integer> {
}