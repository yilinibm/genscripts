package com.genscript.leads.controller;

import com.genscript.leads.dto.AgentContextDtos;
import com.genscript.leads.dto.EmailDtos;
import com.genscript.leads.service.AgentContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent-context")
@RequiredArgsConstructor
public class AgentContextController {
    private final AgentContextService service;

    @GetMapping("/product-bundles/search")
    public List<AgentContextDtos.ProductBundleCandidate> searchProductBundles(
            @RequestParam(name = "q", defaultValue = "") String query,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return service.searchProductBundles(query, limit);
    }

    @GetMapping("/leads/match")
    public List<AgentContextDtos.LeadContextMatch> matchLeads(
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) String counterpartyEmail,
            @RequestParam(required = false) String productBundleCode,
            @RequestParam(required = false) String subject,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return service.matchLeads(customerEmail, counterpartyEmail, productBundleCode, subject, limit);
    }

    @GetMapping("/emails/by-provider/{providerEmailId}")
    public EmailDtos.EmailResponse emailByProvider(@PathVariable String providerEmailId) {
        return service.emailByProvider(providerEmailId);
    }
}
