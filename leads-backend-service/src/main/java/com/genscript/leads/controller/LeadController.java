package com.genscript.leads.controller;

import com.genscript.leads.dto.LeadActivitySummaryDtos;
import com.genscript.leads.dto.LeadDtos;
import com.genscript.leads.dto.TimelineDtos;
import com.genscript.leads.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {
    private final StorageService service;

    @PostMapping
    public LeadDtos.LeadResponse create(@RequestBody LeadDtos.LeadRequest request) {
        return service.upsertLead(request);
    }

    @PutMapping("/{id}")
    public LeadDtos.LeadResponse update(@PathVariable UUID id, @RequestBody LeadDtos.LeadRequest request) {
        return service.upsertLead(new LeadDtos.LeadRequest(
                id, request.leadNo(), request.customerEmail(), request.customerEmailNormalized(), request.customerName(),
                request.company(), request.title(), request.phone(), request.productBundleId(), request.productBundleCode(),
                request.leadUniqueKey(), request.ownerSalesEmail(), request.status(), request.intentLevel(), request.currentStage(),
                request.timelineTrend(), request.latestTimelineSummaryId(), request.source(), request.inquirySummary(),
                request.extractedRequirements(), request.firstEmailId(), request.latestEmailAt(), request.lastCustomerEmailAt(),
                request.lastSalesActivityAt(), request.createdBy()
        ));
    }

    @GetMapping
    public Page<LeadDtos.LeadResponse> list(Pageable pageable) {
        return service.listLeads(pageable);
    }

    @GetMapping("/{id}")
    public LeadDtos.LeadResponse get(@PathVariable UUID id) {
        return service.getLead(id);
    }

    @GetMapping("/{id}/timeline")
    public TimelineDtos.TimelineResponse timeline(@PathVariable UUID id) {
        return service.timeline(id);
    }

    @GetMapping("/{id}/activity-summary")
    public LeadActivitySummaryDtos.LeadActivitySummaryResponse latestSummary(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "LAST_30_DAYS") String summaryWindow
    ) {
        return service.getLatestSummary(id, summaryWindow);
    }
}
