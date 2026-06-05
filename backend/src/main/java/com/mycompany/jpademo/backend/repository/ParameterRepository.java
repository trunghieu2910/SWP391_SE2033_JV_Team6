package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParameterRepository extends JpaRepository<Parameter,Integer> {

}
