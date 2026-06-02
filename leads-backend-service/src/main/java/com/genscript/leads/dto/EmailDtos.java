package com.genscript.leads.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class EmailDtos {
    private EmailDtos() {
    }

    public record EmailRequest(
            UUID id,
            String providerEmailId,
            String mailbox,
            String threadId,
            String direction,
            String fromEmail,
            String fromName,
            List<String> toEmails,
            List<String> ccEmails,
            String subject,
            String bodyText,
            String bodyHtmlRef,
            String snippet,
            List<Object> attachmentRefs,
            Instant sentAt,
            Instant receivedAt,
            Instant processedAt,
            String emailStatus,
            String rawStorageRef
    ) {
    }

    public record EmailResponse(
            UUID id,
            String providerEmailId,
            String mailbox,
            String threadId,
            String direction,
            String fromEmail,
            String fromName,
            List<String> toEmails,
            List<String> ccEmails,
            String subject,
            String bodyText,
            String bodyHtmlRef,
            String snippet,
            List<Object> attachmentRefs,
            Instant sentAt,
            Instant receivedAt,
            Instant processedAt,
            String emailStatus,
            String rawStorageRef,
            Instant createdAt
    ) {
    }
}
