package com.genscript.leads.controller;

import com.genscript.leads.dto.TaskDtos;
import com.genscript.leads.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/follow-up-tasks")
@RequiredArgsConstructor
public class TaskController {
    private final StorageService service;

    @PostMapping
    public TaskDtos.TaskResponse create(@RequestBody TaskDtos.TaskRequest request) {
        return service.upsertTask(request);
    }

    @PutMapping("/{id}")
    public TaskDtos.TaskResponse update(@PathVariable UUID id, @RequestBody TaskDtos.TaskRequest request) {
        return service.upsertTask(new TaskDtos.TaskRequest(
                id, request.taskNo(), request.leadId(), request.sourceEmailId(), request.assignedSalesEmail(), request.status(),
                request.taskType(), request.priority(), request.title(), request.summary(), request.reason(), request.suggestedAction(),
                request.displaySummary(), request.customerNeedSummary(), request.sourceEventSummary(), request.actionItems(),
                request.contextSnapshot(), request.priorityReason(), request.dueAt(), request.acceptedAt(), request.completedAt(),
                request.dismissedAt(), request.closeReason(), request.createdBy()
        ));
    }

    @GetMapping
    public Page<TaskDtos.TaskResponse> list(
            @RequestParam(required = false) String assignedSalesEmail,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID leadId,
            Pageable pageable
    ) {
        return service.listTasks(assignedSalesEmail, status, leadId, pageable);
    }

    @GetMapping("/{id}")
    public TaskDtos.TaskResponse get(@PathVariable UUID id) {
        return service.getTask(id);
    }
}
