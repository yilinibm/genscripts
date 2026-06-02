package com.genscript.leads.controller;

import com.genscript.leads.dto.MailboxBusinessDtos;
import com.genscript.leads.service.MailboxBusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mailbox-business")
@RequiredArgsConstructor
public class MailboxBusinessController {
    private final MailboxBusinessService service;

    @PostMapping("/leads")
    public MailboxBusinessDtos.LeadFromMailboxEmailResponse createLead(@RequestBody MailboxBusinessDtos.LeadFromMailboxEmailRequest request) {
        return service.createLead(request);
    }

    @PostMapping("/follow-up-tasks")
    public MailboxBusinessDtos.TaskFromMailboxEmailResponse createTask(@RequestBody MailboxBusinessDtos.TaskFromMailboxEmailRequest request) {
        return service.createTask(request);
    }

    @PostMapping("/sales-activities")
    public MailboxBusinessDtos.ActivityFromMailboxEmailResponse createActivity(@RequestBody MailboxBusinessDtos.ActivityFromMailboxEmailRequest request) {
        return service.createActivity(request);
    }
}
