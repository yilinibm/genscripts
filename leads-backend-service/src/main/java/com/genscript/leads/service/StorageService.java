package com.genscript.leads.service;

import com.genscript.leads.domain.*;
import com.genscript.leads.dto.*;
import com.genscript.leads.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final ProductBundleService productBundleService;
    private final LeadRecordRepository leadRepository;
    private final EmailMessageRepository emailRepository;
    private final LeadEmailRepository leadEmailRepository;
    private final FollowUpTaskRepository taskRepository;
    private final SalesActivityRepository activityRepository;
    private final LeadActivitySummaryRepository summaryRepository;
    private final ProductBundleRepository productBundleRepository;
    private final Mapper mapper;

    @Transactional
    public LeadDtos.LeadResponse upsertLead(LeadDtos.LeadRequest request) {
        LeadRecord entity = findLead(request).orElseGet(LeadRecord::new);
        boolean isNew = entity.getId() == null;
        ProductBundle productBundle = resolveProductBundle(request.productBundleId(), request.productBundleCode());
        entity.setId(entity.getId() == null ? Objects.requireNonNullElseGet(request.id(), UUID::randomUUID) : entity.getId());
        entity.setLeadNo(required(request.leadNo(), "leadNo"));
        entity.setCustomerEmail(required(request.customerEmail(), "customerEmail"));
        entity.setCustomerEmailNormalized(defaultString(request.customerEmailNormalized(), request.customerEmail().toLowerCase(Locale.ROOT)));
        entity.setCustomerName(request.customerName());
        entity.setCompany(request.company());
        entity.setTitle(request.title());
        entity.setPhone(request.phone());
        entity.setProductBundle(productBundle);
        entity.setLeadUniqueKey(defaultString(request.leadUniqueKey(), entity.getCustomerEmailNormalized() + " + " + productBundle.getPathEn()));
        entity.setOwnerSalesEmail(required(request.ownerSalesEmail(), "ownerSalesEmail"));
        entity.setStatus(required(request.status(), "status"));
        entity.setIntentLevel(request.intentLevel());
        entity.setCurrentStage(request.currentStage());
        entity.setTimelineTrend(request.timelineTrend());
        entity.setLatestTimelineSummaryId(request.latestTimelineSummaryId());
        entity.setSource(defaultString(request.source(), "EMAIL"));
        entity.setInquirySummary(request.inquirySummary());
        entity.setExtractedRequirements(mapOrEmpty(request.extractedRequirements()));
        entity.setFirstEmailId(request.firstEmailId());
        entity.setLatestEmailAt(request.latestEmailAt());
        entity.setLastCustomerEmailAt(request.lastCustomerEmailAt());
        entity.setLastSalesActivityAt(request.lastSalesActivityAt());
        entity.setCreatedBy(defaultString(request.createdBy(), "LEADS_AGENT"));
        if (isNew) {
            entity.setCreatedAt(Instant.now());
        }
        entity.setUpdatedAt(Instant.now());
        return mapper.lead(leadRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<LeadDtos.LeadResponse> listLeads(Pageable pageable) {
        return leadRepository.findAll(pageable).map(mapper::lead);
    }

    @Transactional(readOnly = true)
    public LeadDtos.LeadResponse getLead(UUID id) {
        return mapper.lead(getLeadEntity(id));
    }

    @Transactional
    public EmailDtos.EmailResponse upsertEmail(EmailDtos.EmailRequest request) {
        EmailMessage entity = findEmail(request).orElseGet(EmailMessage::new);
        boolean isNew = entity.getId() == null;
        entity.setId(entity.getId() == null ? Objects.requireNonNullElseGet(request.id(), UUID::randomUUID) : entity.getId());
        entity.setProviderEmailId(required(request.providerEmailId(), "providerEmailId"));
        entity.setMailbox(required(request.mailbox(), "mailbox"));
        entity.setThreadId(request.threadId());
        entity.setDirection(required(request.direction(), "direction"));
        entity.setFromEmail(required(request.fromEmail(), "fromEmail"));
        entity.setFromName(request.fromName());
        entity.setToEmails(listOrEmpty(request.toEmails()));
        entity.setCcEmails(listOrEmpty(request.ccEmails()));
        entity.setSubject(request.subject());
        entity.setBodyText(request.bodyText());
        entity.setBodyHtmlRef(request.bodyHtmlRef());
        entity.setSnippet(request.snippet());
        entity.setAttachmentRefs(objectListOrEmpty(request.attachmentRefs()));
        entity.setSentAt(request.sentAt());
        entity.setReceivedAt(request.receivedAt());
        entity.setProcessedAt(request.processedAt());
        entity.setEmailStatus(request.emailStatus());
        entity.setRawStorageRef(request.rawStorageRef());
        if (isNew) {
            entity.setCreatedAt(Instant.now());
        }
        return mapper.email(emailRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<EmailDtos.EmailResponse> listEmails(Pageable pageable) {
        return emailRepository.findAll(pageable).map(mapper::email);
    }

    @Transactional(readOnly = true)
    public EmailDtos.EmailResponse getEmail(UUID id) {
        return mapper.email(getEmailEntity(id));
    }

    @Transactional
    public LeadEmailDtos.LeadEmailResponse upsertLeadEmail(LeadEmailDtos.LeadEmailRequest request) {
        UUID leadId = Objects.requireNonNull(request.leadId(), "leadId is required");
        UUID emailId = Objects.requireNonNull(request.emailMessageId(), "emailMessageId is required");
        LeadEmail entity = request.id() != null
                ? leadEmailRepository.findById(request.id()).orElseGet(LeadEmail::new)
                : leadEmailRepository.findByLeadIdAndEmailMessageId(leadId, emailId).orElseGet(LeadEmail::new);
        boolean isNew = entity.getId() == null;
        entity.setId(entity.getId() == null ? Objects.requireNonNullElseGet(request.id(), UUID::randomUUID) : entity.getId());
        entity.setLead(getLeadEntity(leadId));
        entity.setEmailMessage(getEmailEntity(emailId));
        entity.setRelationType(required(request.relationType(), "relationType"));
        entity.setMatchConfidence(request.matchConfidence());
        entity.setMatchReason(request.matchReason());
        if (isNew) {
            entity.setCreatedAt(Instant.now());
        }
        return mapper.leadEmail(leadEmailRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<LeadEmailDtos.LeadEmailResponse> listLeadEmails(UUID leadId) {
        return leadEmailRepository.findByLeadIdOrderByCreatedAtDesc(leadId).stream().map(mapper::leadEmail).toList();
    }

    @Transactional
    public void deleteLeadEmail(UUID id) {
        if (!leadEmailRepository.existsById(id)) {
            throw new NotFoundException("Lead email relation not found: " + id);
        }
        leadEmailRepository.deleteById(id);
    }

    @Transactional
    public TaskDtos.TaskResponse upsertTask(TaskDtos.TaskRequest request) {
        FollowUpTask entity = findTask(request).orElseGet(FollowUpTask::new);
        boolean isNew = entity.getId() == null;
        entity.setId(entity.getId() == null ? Objects.requireNonNullElseGet(request.id(), UUID::randomUUID) : entity.getId());
        entity.setTaskNo(required(request.taskNo(), "taskNo"));
        entity.setLead(getLeadEntity(request.leadId()));
        entity.setSourceEmail(request.sourceEmailId() == null ? null : getEmailEntity(request.sourceEmailId()));
        entity.setAssignedSalesEmail(required(request.assignedSalesEmail(), "assignedSalesEmail"));
        entity.setStatus(required(request.status(), "status"));
        entity.setTaskType(required(request.taskType(), "taskType"));
        entity.setPriority(defaultString(request.priority(), "NORMAL"));
        entity.setTitle(required(request.title(), "title"));
        entity.setSummary(request.summary());
        entity.setReason(request.reason());
        entity.setSuggestedAction(request.suggestedAction());
        entity.setDisplaySummary(request.displaySummary());
        entity.setCustomerNeedSummary(request.customerNeedSummary());
        entity.setSourceEventSummary(request.sourceEventSummary());
        entity.setActionItems(listOrEmpty(request.actionItems()));
        entity.setContextSnapshot(mapOrEmpty(request.contextSnapshot()));
        entity.setPriorityReason(request.priorityReason());
        entity.setDueAt(request.dueAt());
        entity.setAcceptedAt(request.acceptedAt());
        entity.setCompletedAt(request.completedAt());
        entity.setDismissedAt(request.dismissedAt());
        entity.setCloseReason(request.closeReason());
        entity.setCreatedBy(defaultString(request.createdBy(), "LEADS_AGENT"));
        if (isNew) {
            entity.setCreatedAt(Instant.now());
        }
        entity.setUpdatedAt(Instant.now());
        return mapper.task(taskRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<TaskDtos.TaskResponse> listTasks(String assignedSalesEmail, String status, UUID leadId, Pageable pageable) {
        Page<FollowUpTask> page;
        if (leadId != null) {
            page = taskRepository.findByLeadId(leadId, pageable);
        } else if (assignedSalesEmail != null && status != null) {
            page = taskRepository.findByAssignedSalesEmailAndStatus(assignedSalesEmail, status, pageable);
        } else {
            page = taskRepository.findAll(pageable);
        }
        return page.map(mapper::task);
    }

    @Transactional(readOnly = true)
    public TaskDtos.TaskResponse getTask(UUID id) {
        return mapper.task(getTaskEntity(id));
    }

    @Transactional
    public SalesActivityDtos.SalesActivityResponse upsertSalesActivity(SalesActivityDtos.SalesActivityRequest request) {
        SalesActivity entity = findActivity(request).orElseGet(SalesActivity::new);
        boolean isNew = entity.getId() == null;
        entity.setId(entity.getId() == null ? Objects.requireNonNullElseGet(request.id(), UUID::randomUUID) : entity.getId());
        entity.setActivityNo(required(request.activityNo(), "activityNo"));
        entity.setLead(getLeadEntity(request.leadId()));
        entity.setSourceEmail(request.sourceEmailId() == null ? null : getEmailEntity(request.sourceEmailId()));
        entity.setRelatedTask(request.relatedTaskId() == null ? null : getTaskEntity(request.relatedTaskId()));
        entity.setSalesEmail(required(request.salesEmail(), "salesEmail"));
        entity.setActivityType(required(request.activityType(), "activityType"));
        entity.setOccurredAt(Objects.requireNonNull(request.occurredAt(), "occurredAt is required"));
        entity.setTitle(request.title());
        entity.setSummary(required(request.summary(), "summary"));
        entity.setKeyPoints(listOrEmpty(request.keyPoints()));
        entity.setCustomerSignals(listOrEmpty(request.customerSignals()));
        entity.setNextStepSignals(listOrEmpty(request.nextStepSignals()));
        entity.setProgressSignal(defaultString(request.progressSignal(), "UNKNOWN"));
        entity.setProgressReason(request.progressReason());
        entity.setStageAfterActivity(request.stageAfterActivity());
        entity.setExtractedPayload(mapOrEmpty(request.extractedPayload()));
        entity.setConfidence(request.confidence());
        entity.setCreatedBy(defaultString(request.createdBy(), "LEADS_AGENT"));
        if (isNew) {
            entity.setCreatedAt(Instant.now());
        }
        entity.setUpdatedAt(Instant.now());
        return mapper.salesActivity(activityRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<SalesActivityDtos.SalesActivityResponse> listActivities(UUID leadId) {
        return activityRepository.findByLeadIdOrderByOccurredAtDesc(leadId).stream().map(mapper::salesActivity).toList();
    }

    @Transactional
    public LeadActivitySummaryDtos.LeadActivitySummaryResponse upsertSummary(LeadActivitySummaryDtos.LeadActivitySummaryRequest request) {
        UUID leadId = Objects.requireNonNull(request.leadId(), "leadId is required");
        Instant windowEndAt = Objects.requireNonNull(request.windowEndAt(), "windowEndAt is required");
        LeadActivitySummary entity = request.id() != null
                ? summaryRepository.findById(request.id()).orElseGet(LeadActivitySummary::new)
                : summaryRepository.findByLeadIdAndSummaryWindowAndWindowEndAt(leadId, required(request.summaryWindow(), "summaryWindow"), windowEndAt).orElseGet(LeadActivitySummary::new);
        entity.setId(entity.getId() == null ? Objects.requireNonNullElseGet(request.id(), UUID::randomUUID) : entity.getId());
        entity.setLead(getLeadEntity(leadId));
        entity.setSummaryWindow(required(request.summaryWindow(), "summaryWindow"));
        entity.setWindowStartAt(Objects.requireNonNull(request.windowStartAt(), "windowStartAt is required"));
        entity.setWindowEndAt(windowEndAt);
        entity.setOverallSummary(required(request.overallSummary(), "overallSummary"));
        entity.setCustomerIntent(defaultString(request.customerIntent(), "UNCLEAR"));
        entity.setCurrentStage(required(request.currentStage(), "currentStage"));
        entity.setTrend(required(request.trend(), "trend"));
        entity.setTrendReason(request.trendReason());
        entity.setProgressActivityCount(request.progressActivityCount() == null ? 0 : request.progressActivityCount());
        entity.setNoProgressActivityCount(request.noProgressActivityCount() == null ? 0 : request.noProgressActivityCount());
        entity.setLastProgressAt(request.lastProgressAt());
        entity.setLastActivityAt(request.lastActivityAt());
        entity.setNextRecommendedAction(request.nextRecommendedAction());
        entity.setSourceActivityIds(request.sourceActivityIds() == null ? new ArrayList<>() : request.sourceActivityIds());
        entity.setConfidence(request.confidence());
        entity.setGeneratedBy(defaultString(request.generatedBy(), "LEADS_AGENT"));
        entity.setGeneratedAt(request.generatedAt() == null ? Instant.now() : request.generatedAt());
        return mapper.leadActivitySummary(summaryRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public LeadActivitySummaryDtos.LeadActivitySummaryResponse getLatestSummary(UUID leadId, String summaryWindow) {
        return summaryRepository.findFirstByLeadIdAndSummaryWindowOrderByGeneratedAtDesc(leadId, defaultString(summaryWindow, "LAST_30_DAYS"))
                .map(mapper::leadActivitySummary)
                .orElseThrow(() -> new NotFoundException("Lead activity summary not found"));
    }

    @Transactional(readOnly = true)
    public TimelineDtos.TimelineResponse timeline(UUID leadId) {
        LeadActivitySummaryDtos.LeadActivitySummaryResponse summary = summaryRepository
                .findFirstByLeadIdAndSummaryWindowOrderByGeneratedAtDesc(leadId, "LAST_30_DAYS")
                .map(mapper::leadActivitySummary)
                .orElse(null);
        List<TimelineDtos.TimelineItem> items = new ArrayList<>();
        for (LeadEmail leadEmail : leadEmailRepository.findByLeadIdOrderByCreatedAtDesc(leadId)) {
            EmailMessage email = leadEmail.getEmailMessage();
            Instant occurredAt = email.getReceivedAt() != null ? email.getReceivedAt() : email.getSentAt();
            items.add(new TimelineDtos.TimelineItem("EMAIL", email.getId(), occurredAt, email.getSnippet(), Map.of(
                    "direction", email.getDirection(),
                    "subject", email.getSubject(),
                    "relationType", leadEmail.getRelationType()
            )));
        }
        for (FollowUpTask task : taskRepository.findByLeadIdOrderByCreatedAtDesc(leadId)) {
            items.add(new TimelineDtos.TimelineItem("FOLLOW_UP_TASK", task.getId(), task.getCreatedAt(), task.getSummary(), Map.of(
                    "title", task.getTitle(),
                    "status", task.getStatus(),
                    "taskType", task.getTaskType(),
                    "priority", task.getPriority()
            )));
        }
        for (SalesActivity activity : activityRepository.findByLeadIdOrderByOccurredAtDesc(leadId)) {
            items.add(new TimelineDtos.TimelineItem("SALES_ACTIVITY", activity.getId(), activity.getOccurredAt(), activity.getSummary(), Map.of(
                    "title", activity.getTitle() == null ? "" : activity.getTitle(),
                    "activityType", activity.getActivityType(),
                    "progressSignal", activity.getProgressSignal()
            )));
        }
        items.sort(Comparator.comparing(TimelineDtos.TimelineItem::occurredAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return new TimelineDtos.TimelineResponse(leadId, summary, items);
    }

    private Optional<LeadRecord> findLead(LeadDtos.LeadRequest request) {
        if (request.id() != null) return leadRepository.findById(request.id());
        if (request.leadNo() != null) return leadRepository.findByLeadNo(request.leadNo());
        if (request.customerEmailNormalized() != null && request.productBundleId() != null) {
            return leadRepository.findByCustomerEmailNormalizedAndProductBundleId(request.customerEmailNormalized(), request.productBundleId());
        }
        return Optional.empty();
    }

    private Optional<EmailMessage> findEmail(EmailDtos.EmailRequest request) {
        if (request.id() != null) return emailRepository.findById(request.id());
        if (request.providerEmailId() != null) return emailRepository.findByProviderEmailId(request.providerEmailId());
        return Optional.empty();
    }

    private Optional<FollowUpTask> findTask(TaskDtos.TaskRequest request) {
        if (request.id() != null) return taskRepository.findById(request.id());
        if (request.taskNo() != null) return taskRepository.findByTaskNo(request.taskNo());
        return Optional.empty();
    }

    private Optional<SalesActivity> findActivity(SalesActivityDtos.SalesActivityRequest request) {
        if (request.id() != null) return activityRepository.findById(request.id());
        if (request.activityNo() != null) return activityRepository.findByActivityNo(request.activityNo());
        return Optional.empty();
    }

    private ProductBundle resolveProductBundle(UUID id, String code) {
        if (id != null) return productBundleService.getEntity(id);
        if (code != null && !code.isBlank()) return productBundleService.getEntityByCode(code);
        throw new BadRequestException("Either productBundleId or productBundleCode is required");
    }

    private LeadRecord getLeadEntity(UUID id) {
        if (id == null) throw new BadRequestException("leadId is required");
        return leadRepository.findById(id).orElseThrow(() -> new NotFoundException("Lead not found: " + id));
    }

    private EmailMessage getEmailEntity(UUID id) {
        return emailRepository.findById(id).orElseThrow(() -> new NotFoundException("Email not found: " + id));
    }

    private FollowUpTask getTaskEntity(UUID id) {
        return taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Task not found: " + id));
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new BadRequestException("Missing required field: " + field);
        return value.trim();
    }

    private static String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static <T> List<T> listOrEmpty(List<T> value) {
        return value == null ? new ArrayList<>() : value;
    }

    private static List<Object> objectListOrEmpty(List<Object> value) {
        return value == null ? new ArrayList<>() : value;
    }

    private static Map<String, Object> mapOrEmpty(Map<String, Object> value) {
        return value == null ? new LinkedHashMap<>() : value;
    }
}
