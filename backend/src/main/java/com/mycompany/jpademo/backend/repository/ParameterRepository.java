package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access for the shared, system-wide lab parameter catalog.
 */
@Repository
public interface ParameterRepository extends JpaRepository<Parameter, Integer> {

    /**
     * Matches an incoming parameter name (from the LIS payload) against
     * an existing catalog entry. Case-insensitive — note this does NOT
     * normalize whitespace, so near-duplicate names may still create
     * separate catalog entries.
     */
    Optional<Parameter> findByParameterNameIgnoreCase(String parameterName);
}

