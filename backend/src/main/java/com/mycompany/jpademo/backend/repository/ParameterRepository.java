package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParameterRepository extends JpaRepository<Parameter, Integer> {

    /**
     * Dùng để khớp testName từ LIS với Parameter đã có trong danh mục.
     *
     * Tìm kiếm không phân biệt hoa/thường.
     */
    Optional<Parameter> findByParameterNameIgnoreCase(String parameterName);
}

