package com.codewise.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryRequestDto {
    private String language;
    private String purpose;
    private LocalDateTime createdAt;
    private List<ErrorInfo> errors;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorInfo {
        private String type;
        private String message;
    }
}
