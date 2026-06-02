package com.genscript.leads.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TaskDtos {
    private TaskDtos() {
    }

    public record TaskRequest(
            UUID id,
            String taskNo,
            UUID leadId,
            UUID sourceEmailId,
            String assignedSalesEmail,
            String status,
            String taskType,
            String priority,
            String title,
            String summary,
            String reason,
            String suggestedAction,
            String displaySummary,
            String customerNeedSummary,
            String sourceEventSummary,
            List<String> actionItems,
            Map<String, Object> contextSnapshot,
            String priorityReason,
            Instant dueAt,
            Instant acceptedAt,
            Instant completedAt,
            Instant dismissedAt,
            String closeReason,
            String createdBy
    ) {
    }

    public record TaskResponse(
            UUID id,
            String taskNo,
            UUID leadId,
            UUID sourceEmailId,
            String assignedSalesEmail,
            String status,
            String taskType,
            String priority,
            String title,
            String summary,
            String reason,
            String suggestedAction,
            String displaySummary,
            String customerNeedSummary,
            String sourceEventSummary,
            List<String> actionItems,
            Map<String, Object> contextSnapshot,
            String priorityReason,
            Instant dueAt,
            Instant acceptedAt,
            Instant completedAt,
            Instant dismissedAt,
            String closeReason,
            String createdBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
