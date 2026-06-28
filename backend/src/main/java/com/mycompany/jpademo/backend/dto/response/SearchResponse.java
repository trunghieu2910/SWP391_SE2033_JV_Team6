package com.mycompany.jpademo.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SearchResponse {
    private List<UserSearchDTO> users;
    private List<LogSearchDTO> logs;
}