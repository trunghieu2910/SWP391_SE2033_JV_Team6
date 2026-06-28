package com.mycompany.jpademo.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class IpRequestStats {
    private String ipAddress;
    private Long requestCount;
}
