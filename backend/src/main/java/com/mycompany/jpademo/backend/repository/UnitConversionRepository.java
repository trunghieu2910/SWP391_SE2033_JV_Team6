package com.mycompany.jpademo.backend.repository;

import com.mycompany.jpademo.backend.entity.UnitConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitConversionRepository extends JpaRepository<UnitConversion, Integer> {
    @Query("SELECT uc FROM UnitConversion uc WHERE uc.drug.drugId = :drugId")
    List<UnitConversion> findByDrugId(@Param("drugId") Integer drugId);

    @Query("SELECT uc FROM UnitConversion uc WHERE uc.drug.drugId = :drugId AND uc.largeUnit.unitId = :largeUnitId AND uc.smallUnit.unitId = :smallUnitId")
    Optional<UnitConversion> findByDrugIdAndLargeUnitIdAndSmallUnitId(
        @Param("drugId") Integer drugId,
        @Param("largeUnitId") Integer largeUnitId,
        @Param("smallUnitId") Integer smallUnitId
    );
}
