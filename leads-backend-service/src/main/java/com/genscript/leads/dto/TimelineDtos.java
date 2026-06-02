package com.genscript.leads.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TimelineDtos {
    private TimelineDtos() {
    }

    public record TimelineResponse(
            UUID leadId,
            LeadActivitySummaryDtos.LeadActivitySummaryResponse activitySummary,
            List<TimelineItem> items
    ) {
    }

    public record TimelineItem(
            String type,
            UUID id,
            Instant occurredAt,
            String summary,
            Map<String, Object> display
    ) {
    }
}
