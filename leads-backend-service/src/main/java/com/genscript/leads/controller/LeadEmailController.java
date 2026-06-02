package com.genscript.leads.controller;

import com.genscript.leads.dto.LeadEmailDtos;
import com.genscript.leads.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class LeadEmailController {
    private final StorageService service;

    @PostMapping("/api/lead-emails")
    public LeadEmailDtos.LeadEmailResponse create(@RequestBody LeadEmailDtos.LeadEmailRequest request) {
        return service.upsertLeadEmail(request);
    }

    @DeleteMapping("/api/lead-emails/{id}")
    public void delete(@PathVariable UUID id) {
        service.deleteLeadEmail(id);
    }

    @GetMapping("/api/leads/{leadId}/emails")
    public List<LeadEmailDtos.LeadEmailResponse> list(@PathVariable UUID leadId) {
        return service.listLeadEmails(leadId);
    }
}
