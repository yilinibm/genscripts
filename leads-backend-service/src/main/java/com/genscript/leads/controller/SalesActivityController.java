package com.genscript.leads.controller;

import com.genscript.leads.dto.SalesActivityDtos;
import com.genscript.leads.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SalesActivityController {
    private final StorageService service;

    @PostMapping("/api/sales-activities")
    public SalesActivityDtos.SalesActivityResponse create(@RequestBody SalesActivityDtos.SalesActivityRequest request) {
        return service.upsertSalesActivity(request);
    }

    @PutMapping("/api/sales-activities/{id}")
    public SalesActivityDtos.SalesActivityResponse update(@PathVariable UUID id, @RequestBody SalesActivityDtos.SalesActivityRequest request) {
        return service.upsertSalesActivity(new SalesActivityDtos.SalesActivityRequest(
                id, request.activityNo(), request.leadId(), request.sourceEmailId(), request.relatedTaskId(), request.salesEmail(),
                request.activityType(), request.occurredAt(), request.title(), request.summary(), request.keyPoints(),
                request.customerSignals(), request.nextStepSignals(), request.progressSignal(), request.progressReason(),
                request.stageAfterActivity(), request.extractedPayload(), request.confidence(), request.createdBy()
        ));
    }

    @GetMapping("/api/leads/{leadId}/activities")
    public List<SalesActivityDtos.SalesActivityResponse> list(@PathVariable UUID leadId) {
        return service.listActivities(leadId);
    }
}
