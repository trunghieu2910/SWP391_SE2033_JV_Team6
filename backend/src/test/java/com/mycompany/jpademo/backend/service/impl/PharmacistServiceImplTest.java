package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.entity.Unit;
import com.mycompany.jpademo.backend.entity.UnitConversion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PharmacistServiceImplTest {

    @Test
    void calculateSmallUnitQuantityFromConversions_shouldFollowMultiStepChain() {
        Unit box = Unit.builder().unitId(3).unitName("HỘP").build();
        Unit strip = Unit.builder().unitId(2).unitName("VỈ").build();
        Unit tablet = Unit.builder().unitId(1).unitName("VIÊN").build();

        List<UnitConversion> conversions = List.of(
            UnitConversion.builder().largeUnit(box).smallUnit(strip).conversionQuantity(3).build(),
            UnitConversion.builder().largeUnit(strip).smallUnit(tablet).conversionQuantity(10).build()
        );

        int factor = PharmacistServiceImpl.calculateSmallUnitQuantityFromConversions(conversions, 3, 2);

        assertEquals(60, factor);
    }

    @Test
    void calculateSmallUnitQuantityFromChain_shouldMultiplyAllEnteredSteps() {
        int factor = PharmacistServiceImpl.calculateSmallUnitQuantityFromChain("3,10", 2);

        assertEquals(60, factor);
    }
}
