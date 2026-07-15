package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Integer> {
    Optional<Unit> findByUnitName(String unitName);
}
