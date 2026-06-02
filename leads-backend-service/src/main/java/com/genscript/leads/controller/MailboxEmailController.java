package com.genscript.leads.controller;

import com.genscript.leads.dto.MailboxEmailDtos;
import com.genscript.leads.service.MailboxEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/mailbox-emails")
@RequiredArgsConstructor
public class MailboxEmailController {
    private final MailboxEmailService service;

    @PostMapping("/upsert-batch")
    public MailboxEmailDtos.MailboxEmailBatchResponse upsertBatch(@RequestBody MailboxEmailDtos.MailboxEmailBatchRequest request) {
        return service.upsertBatch(request);
    }

    @GetMapping
    public Page<MailboxEmailDtos.MailboxEmailResponse> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/pending")
    public Page<MailboxEmailDtos.MailboxEmailResponse> pending(
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String mailbox,
            Pageable pageable
    ) {
        return service.pending(direction, mailbox, pageable);
    }

    @GetMapping("/{id}")
    public MailboxEmailDtos.MailboxEmailResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping("/{id}/mark-processed")
    public MailboxEmailDtos.MailboxEmailResponse markProcessed(
            @PathVariable UUID id,
            @RequestBody(required = false) MailboxEmailDtos.MailboxEmailStatusRequest request
    ) {
        return service.markProcessed(id, request == null ? null : request.reason());
    }

    @PostMapping("/{id}/mark-ignored")
    public MailboxEmailDtos.MailboxEmailResponse markIgnored(
            @PathVariable UUID id,
            @RequestBody(required = false) MailboxEmailDtos.MailboxEmailStatusRequest request
    ) {
        return service.markIgnored(id, request == null ? null : request.reason());
    }
}
