package com.mycompany.jpademo.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class EndpointRequestStats {
    private String uri;
    private String method;
    private Long requestCount;
}
