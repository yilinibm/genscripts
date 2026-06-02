package com.genscript.leads.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MailboxBusinessDtos {
    private MailboxBusinessDtos() {
    }

    public record LeadFromMailboxEmailRequest(
            UUID mailboxEmailId,
            String productBundleCode,
            String ownerSalesEmail,
            String customerName,
            String intentLevel,
            String currentStage,
            String timelineTrend,
            String inquirySummary,
            Map<String, Object> extractedRequirements,
            BigDecimal confidence,
            String matchReason
    ) {
    }

    public record LeadFromMailboxEmailResponse(
            boolean createdNewLead,
            LeadDtos.LeadResponse lead,
            EmailDtos.EmailResponse email,
            LeadEmailDtos.LeadEmailResponse leadEmail,
            MailboxEmailDtos.MailboxEmailResponse mailboxEmail
    ) {
    }

    public record TaskFromMailboxEmailRequest(
            UUID mailboxEmailId,
            UUID leadId,
            String customerEmail,
            String productBundleCode,
            String assignedSalesEmail,
            String taskType,
            String priority,
            String title,
            String summary,
            String reason,
            String suggestedAction,
            List<String> actionItems,
            Map<String, Object> contextSnapshot,
            String priorityReason,
            Instant dueAt
    ) {
    }

    public record TaskFromMailboxEmailResponse(
            boolean deduplicated,
            TaskDtos.TaskResponse task,
            LeadDtos.LeadResponse lead,
            MailboxEmailDtos.MailboxEmailResponse mailboxEmail
    ) {
    }

    public record ActivityFromMailboxEmailRequest(
            UUID mailboxEmailId,
            UUID leadId,
            String customerEmail,
            String productBundleCode,
            String relatedTaskId,
            String activityType,
            String title,
            String summary,
            List<String> keyPoints,
            List<String> customerSignals,
            List<String> nextStepSignals,
            String progressSignal,
            String progressReason,
            String stageAfterActivity,
            Map<String, Object> extractedPayload,
            BigDecimal confidence
    ) {
    }

    public record ActivityFromMailboxEmailResponse(
            boolean deduplicated,
            SalesActivityDtos.SalesActivityResponse activity,
            LeadDtos.LeadResponse lead,
            EmailDtos.EmailResponse email,
            LeadEmailDtos.LeadEmailResponse leadEmail,
            MailboxEmailDtos.MailboxEmailResponse mailboxEmail
    ) {
    }
}
