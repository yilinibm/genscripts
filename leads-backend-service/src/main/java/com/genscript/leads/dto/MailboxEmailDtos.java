package com.genscript.leads.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MailboxEmailDtos {
    private MailboxEmailDtos() {
    }

    public record MailboxEmailRequest(
            UUID id,
            String provider,
            String mailbox,
            String folder,
            String providerUid,
            String messageId,
            String direction,
            String fromEmail,
            String fromName,
            List<String> toEmails,
            List<String> ccEmails,
            String subject,
            String bodyText,
            String snippet,
            Instant sentAt,
            Instant receivedAt,
            String processingStatus,
            String processingReason,
            Map<String, Object> rawPayload
    ) {
    }

    public record MailboxEmailBatchRequest(List<MailboxEmailRequest> emails) {
    }

    public record MailboxEmailStatusRequest(String reason) {
    }

    public record MailboxEmailResponse(
            UUID id,
            String provider,
            String mailbox,
            String folder,
            String providerUid,
            String messageId,
            String dedupeKey,
            String direction,
            String fromEmail,
            String fromName,
            List<String> toEmails,
            List<String> ccEmails,
            String subject,
            String bodyText,
            String snippet,
            Instant sentAt,
            Instant receivedAt,
            Instant syncedAt,
            String processingStatus,
            Instant processedAt,
            String processingReason,
            UUID emailMessageId,
            Map<String, Object> rawPayload,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record MailboxEmailBatchResponse(
            int received,
            int returned,
            List<MailboxEmailResponse> emails
    ) {
    }
}
