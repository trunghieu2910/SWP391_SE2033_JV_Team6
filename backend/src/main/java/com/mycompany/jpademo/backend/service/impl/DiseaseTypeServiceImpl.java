package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.entity.DiseaseType;
import com.mycompany.jpademo.backend.repository.DiseaseTypeRepository;
import com.mycompany.jpademo.backend.service.interfaces.DiseaseTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiseaseTypeServiceImpl implements DiseaseTypeService {

    private final DiseaseTypeRepository diseaseTypeRepository;

    @Override
    public List<DiseaseType> getAllDiseaseTypes() {
        return diseaseTypeRepository.findAll();
    }

}
