package com.genscript.leads.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class LeadActivitySummaryDtos {
    private LeadActivitySummaryDtos() {
    }

    public record LeadActivitySummaryRequest(
            UUID id,
            UUID leadId,
            String summaryWindow,
            Instant windowStartAt,
            Instant windowEndAt,
            String overallSummary,
            String customerIntent,
            String currentStage,
            String trend,
            String trendReason,
            Integer progressActivityCount,
            Integer noProgressActivityCount,
            Instant lastProgressAt,
            Instant lastActivityAt,
            String nextRecommendedAction,
            List<UUID> sourceActivityIds,
            BigDecimal confidence,
            String generatedBy,
            Instant generatedAt
    ) {
    }

    public record LeadActivitySummaryResponse(
            UUID id,
            UUID leadId,
            String summaryWindow,
            Instant windowStartAt,
            Instant windowEndAt,
            String overallSummary,
            String customerIntent,
            String currentStage,
            String trend,
            String trendReason,
            Integer progressActivityCount,
            Integer noProgressActivityCount,
            Instant lastProgressAt,
            Instant lastActivityAt,
            String nextRecommendedAction,
            List<UUID> sourceActivityIds,
            BigDecimal confidence,
            String generatedBy,
            Instant generatedAt
    ) {
    }
}
