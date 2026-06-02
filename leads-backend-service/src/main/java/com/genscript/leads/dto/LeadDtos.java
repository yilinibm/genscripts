package com.genscript.leads.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class LeadDtos {
    private LeadDtos() {
    }

    public record LeadRequest(
            UUID id,
            String leadNo,
            String customerEmail,
            String customerEmailNormalized,
            String customerName,
            String company,
            String title,
            String phone,
            UUID productBundleId,
            String productBundleCode,
            String leadUniqueKey,
            String ownerSalesEmail,
            String status,
            String intentLevel,
            String currentStage,
            String timelineTrend,
            UUID latestTimelineSummaryId,
            String source,
            String inquirySummary,
            Map<String, Object> extractedRequirements,
            UUID firstEmailId,
            Instant latestEmailAt,
            Instant lastCustomerEmailAt,
            Instant lastSalesActivityAt,
            String createdBy
    ) {
    }

    public record LeadResponse(
            UUID id,
            String leadNo,
            String customerEmail,
            String customerEmailNormalized,
            String customerName,
            String company,
            String title,
            String phone,
            ProductBundleDtos.ProductBundleResponse productBundle,
            String leadUniqueKey,
            String ownerSalesEmail,
            String status,
            String intentLevel,
            String currentStage,
            String timelineTrend,
            UUID latestTimelineSummaryId,
            String source,
            String inquirySummary,
            Map<String, Object> extractedRequirements,
            UUID firstEmailId,
            Instant latestEmailAt,
            Instant lastCustomerEmailAt,
            Instant lastSalesActivityAt,
            String createdBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
