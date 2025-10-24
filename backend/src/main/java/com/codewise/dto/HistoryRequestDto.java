package com.codewise.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class HistoryRequestDto {
    private String language;
    private String purpose;
    private List<String> errors;
    private LocalDateTime createdAt;
}
