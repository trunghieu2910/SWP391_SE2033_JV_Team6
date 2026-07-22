package com.mycompany.jpademo.backend.dto.projection;

/**
 * Spring Data JPA interface-based projection for the disease-statistics
 * query in ReviewRepository. Spring Data automatically generates an
 * implementation at runtime that maps each result row's "diseaseName" and
 * "total" aliases (see the @Query in ReviewRepository) onto these getters —
 * no manual mapping code is needed.
 */
public interface DiseaseStatItem {

    /** Name of the disease type this statistic row represents (e.g. "Cervical Cancer Screening"). */
    String getDiseaseName();

    /** Number of Review records for this disease type within the requested date range. */
    Long getTotal();
}
