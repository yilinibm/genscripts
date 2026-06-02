package com.genscript.leads.controller;

import com.genscript.leads.dto.EmailDtos;
import com.genscript.leads.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {
    private final StorageService service;

    @PostMapping
    public EmailDtos.EmailResponse create(@RequestBody EmailDtos.EmailRequest request) {
        return service.upsertEmail(request);
    }

    @PutMapping("/{id}")
    public EmailDtos.EmailResponse update(@PathVariable UUID id, @RequestBody EmailDtos.EmailRequest request) {
        return service.upsertEmail(new EmailDtos.EmailRequest(
                id, request.providerEmailId(), request.mailbox(), request.threadId(), request.direction(),
                request.fromEmail(), request.fromName(), request.toEmails(), request.ccEmails(), request.subject(),
                request.bodyText(), request.bodyHtmlRef(), request.snippet(), request.attachmentRefs(), request.sentAt(),
                request.receivedAt(), request.processedAt(), request.emailStatus(), request.rawStorageRef()
        ));
    }

    @GetMapping
    public Page<EmailDtos.EmailResponse> list(Pageable pageable) {
        return service.listEmails(pageable);
    }

    @GetMapping("/{id}")
    public EmailDtos.EmailResponse get(@PathVariable UUID id) {
        return service.getEmail(id);
    }
}
