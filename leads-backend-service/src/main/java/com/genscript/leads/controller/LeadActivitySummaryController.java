package com.genscript.leads.controller;

import com.genscript.leads.dto.LeadActivitySummaryDtos;
import com.genscript.leads.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/lead-activity-summaries")
@RequiredArgsConstructor
public class LeadActivitySummaryController {
    private final StorageService service;

    @PostMapping
    public LeadActivitySummaryDtos.LeadActivitySummaryResponse create(@RequestBody LeadActivitySummaryDtos.LeadActivitySummaryRequest request) {
        return service.upsertSummary(request);
    }

    @PutMapping("/{id}")
    public LeadActivitySummaryDtos.LeadActivitySummaryResponse update(@PathVariable UUID id, @RequestBody LeadActivitySummaryDtos.LeadActivitySummaryRequest request) {
        return service.upsertSummary(new LeadActivitySummaryDtos.LeadActivitySummaryRequest(
                id, request.leadId(), request.summaryWindow(), request.windowStartAt(), request.windowEndAt(),
                request.overallSummary(), request.customerIntent(), request.currentStage(), request.trend(), request.trendReason(),
                request.progressActivityCount(), request.noProgressActivityCount(), request.lastProgressAt(), request.lastActivityAt(),
                request.nextRecommendedAction(), request.sourceActivityIds(), request.confidence(), request.generatedBy(), request.generatedAt()
        ));
    }
}
