package com.mycompany.jpademo.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

@Getter
@Builder
public class CertificateFileResponse {
    private Resource resource;
    private MediaType mediaType;
    private String displayName;
}