package com.genscript.leads.dto;

import java.util.List;
import java.util.Map;

public final class AgentContextDtos {
    private AgentContextDtos() {
    }

    public record ProductBundleCandidate(
            ProductBundleDtos.ProductBundleResponse productBundle,
            double score,
            String reason
    ) {
    }

    public record LeadContextMatch(
            LeadDtos.LeadResponse lead,
            double score,
            String matchingReason,
            List<TaskDtos.TaskResponse> openTasks,
            List<SalesActivityDtos.SalesActivityResponse> recentActivities,
            Map<String, Long> timelineCounts
    ) {
    }
}
