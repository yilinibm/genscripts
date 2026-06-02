package com.genscript.leads.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SalesActivityDtos {
    private SalesActivityDtos() {
    }

    public record SalesActivityRequest(
            UUID id,
            String activityNo,
            UUID leadId,
            UUID sourceEmailId,
            UUID relatedTaskId,
            String salesEmail,
            String activityType,
            Instant occurredAt,
            String title,
            String summary,
            List<String> keyPoints,
            List<String> customerSignals,
            List<String> nextStepSignals,
            String progressSignal,
            String progressReason,
            String stageAfterActivity,
            Map<String, Object> extractedPayload,
            BigDecimal confidence,
            String createdBy
    ) {
    }

    public record SalesActivityResponse(
            UUID id,
            String activityNo,
            UUID leadId,
            UUID sourceEmailId,
            UUID relatedTaskId,
            String salesEmail,
            String activityType,
            Instant occurredAt,
            String title,
            String summary,
            List<String> keyPoints,
            List<String> customerSignals,
            List<String> nextStepSignals,
            String progressSignal,
            String progressReason,
            String stageAfterActivity,
            Map<String, Object> extractedPayload,
            BigDecimal confidence,
            String createdBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
