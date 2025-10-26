package com.codewise.dto;

import java.util.List;
import java.util.Map;

public record AnalyzeResponse(
        String summary,
        String purpose,
        Map<String, Object> metrics,
        List<Map<String, Object>> issues,
        String inferred_purpose,
        String final_purpose
) {}