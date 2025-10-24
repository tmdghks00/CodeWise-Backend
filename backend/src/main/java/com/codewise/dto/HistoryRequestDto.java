package com.codewise.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class HistoryRequestDto {

    private String language;  // "java"
    private String purpose;   // "security_hardening"
    private List<ErrorInfo> errors;
    private LocalDateTime createdAt;

    @Getter
    @Setter
    public static class ErrorInfo {
        private String type;     // "error", "warn", "info"
        private String message;  // "NPE 가능성"
    }
}
