package com.genscript.leads.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class LeadEmailDtos {
    private LeadEmailDtos() {
    }

    public record LeadEmailRequest(
            UUID id,
            UUID leadId,
            UUID emailMessageId,
            String relationType,
            BigDecimal matchConfidence,
            String matchReason
    ) {
    }

    public record LeadEmailResponse(
            UUID id,
            UUID leadId,
            UUID emailMessageId,
            String relationType,
            BigDecimal matchConfidence,
            String matchReason,
            Instant createdAt
    ) {
    }
}
