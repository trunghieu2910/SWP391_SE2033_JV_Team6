package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.dto.response.ApiResponse;
import com.mycompany.jpademo.backend.dto.response.ParameterResponse;
import com.mycompany.jpademo.backend.repository.ParameterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/parameters")
@RequiredArgsConstructor
public class ParameterController {

    private final ParameterRepository parameterRepository;

    @GetMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse<List<ParameterResponse>>> getAllParameters() {

        List<ParameterResponse> data = parameterRepository.findAll()
                .stream()
                .map(p -> ParameterResponse.builder()
                        .parameterId(p.getParameterId())
                        .parameterName(p.getParameterName())
                        .unit(p.getUnit())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.<List<ParameterResponse>>builder()
                        .code(200)
                        .success(true)
                        .message("Lấy danh sách thông số xét nghiệm thành công")
                        .data(data)
                        .build()
        );
    }
}
