package com.mycompany.jpademo.backend.dto.response;

import com.mycompany.jpademo.backend.entity.BlockedIP;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class GlobalSearchResponse {
    private List<UserSearchDTO> users;
    private List<LogSearchDTO> logs;
    private List<SecuritySearchDTO> blockedIPs;
}