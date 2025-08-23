package com.codewise.dto;

import java.util.List;
import java.util.Map;

public record AnalyzeResponse(
        String summary,
        Map<String, Object> metrics,
        List<Map<String, Object>> issues
) {}
