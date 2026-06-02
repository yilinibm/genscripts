package com.genscript.leads.service;

import com.genscript.leads.domain.*;
import com.genscript.leads.dto.*;
import com.genscript.leads.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MailboxBusinessService {
    private final MailboxEmailService mailboxEmailService;
    private final ProductBundleService productBundleService;
    private final LeadRecordRepository leadRepository;
    private final LeadEmailRepository leadEmailRepository;
    private final FollowUpTaskRepository taskRepository;
    private final SalesActivityRepository activityRepository;
    private final StorageService storageService;
    private final Mapper mapper;
    private final EntityManager entityManager;

    @Transactional
    public MailboxBusinessDtos.LeadFromMailboxEmailResponse createLead(MailboxBusinessDtos.LeadFromMailboxEmailRequest request) {
        MailboxEmail mailboxEmail = mailboxEmailService.getEntity(required(request.mailboxEmailId(), "mailboxEmailId"));
        assertDirection(mailboxEmail, "INBOUND_CUSTOMER", "MAILBOX_EMAIL_NOT_INBOUND_CUSTOMER");
        EmailMessage email = mailboxEmailService.ensureEmailMessage(mailboxEmail.getId());
        ProductBundle product = productBundleService.getEntityByCode(requiredString(request.productBundleCode(), "productBundleCode"));
        String customerEmail = mailboxEmail.getFromEmail().toLowerCase(Locale.ROOT);
        Optional<LeadRecord> existing = leadRepository.findByCustomerEmailNormalizedAndProductBundleId(customerEmail, product.getId());
        boolean created = existing.isEmpty();
        LeadRecord leadEntity;
        if (created) {
            String leadNo = "LEAD-" + shortHash(customerEmail + "|" + product.getCode());
            Map<String, Object> extracted = request.extractedRequirements() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(request.extractedRequirements());
            extracted.put("productBundleCode", product.getCode());
            extracted.put("mailboxEmailId", mailboxEmail.getId().toString());
            extracted.put("mailboxEmailSubject", mailboxEmail.getSubject());
            LeadDtos.LeadResponse createdLead = storageService.upsertLead(new LeadDtos.LeadRequest(
                    null,
                    leadNo,
                    customerEmail,
                    customerEmail,
                    defaultString(request.customerName(), mailboxEmail.getFromName()),
                    null,
                    null,
                    null,
                    null,
                    product.getCode(),
                    customerEmail + " + " + product.getPathEn(),
                    defaultString(request.ownerSalesEmail(), mailboxEmail.getMailbox()),
                    "ACTIVE",
                    defaultString(request.intentLevel(), "MEDIUM"),
                    defaultString(request.currentStage(), "New Lead"),
                    defaultString(request.timelineTrend(), "STABLE"),
                    null,
                    "EMAIL",
                    defaultString(request.inquirySummary(), mailboxEmail.getSnippet()),
                    extracted,
                    email.getId(),
                    occurredAt(mailboxEmail),
                    occurredAt(mailboxEmail),
                    null,
                    "SALES_LEADS_AGENT_V2_LEAD_SKILL"
            ));
            leadEntity = leadRepository.findById(createdLead.id()).orElseThrow();
        } else {
            leadEntity = existing.get();
        }
        LeadEmail leadEmail = leadEmailRepository.findByLeadIdAndEmailMessageId(leadEntity.getId(), email.getId()).orElseGet(LeadEmail::new);
        boolean newRelation = leadEmail.getId() == null;
        leadEmail.setId(leadEmail.getId() == null ? UUID.randomUUID() : leadEmail.getId());
        leadEmail.setLead(leadEntity);
        leadEmail.setEmailMessage(email);
        leadEmail.setRelationType(created ? "FIRST_INQUIRY" : "CUSTOMER_REPLY");
        leadEmail.setMatchConfidence(request.confidence() == null ? BigDecimal.valueOf(0.9) : request.confidence());
        leadEmail.setMatchReason(defaultString(request.matchReason(), "Matched mailbox email by customer email and Agent-selected product bundle"));
        if (newRelation) {
            leadEmail.setCreatedAt(Instant.now());
        }
        leadEmail = leadEmailRepository.save(leadEmail);
        return new MailboxBusinessDtos.LeadFromMailboxEmailResponse(
                created,
                mapper.lead(leadEntity),
                mapper.email(email),
                mapper.leadEmail(leadEmail),
                mapper.mailboxEmail(mailboxEmail)
        );
    }

    @Transactional
    public MailboxBusinessDtos.TaskFromMailboxEmailResponse createTask(MailboxBusinessDtos.TaskFromMailboxEmailRequest request) {
        MailboxEmail mailboxEmail = mailboxEmailService.getEntity(required(request.mailboxEmailId(), "mailboxEmailId"));
        assertDirection(mailboxEmail, "INBOUND_CUSTOMER", "MAILBOX_EMAIL_NOT_INBOUND_CUSTOMER");
        EmailMessage email = mailboxEmailService.ensureEmailMessage(mailboxEmail.getId());
        LeadRecord lead = resolveLead(request.leadId(), request.customerEmail(), request.productBundleCode(), mailboxEmail.getFromEmail(), email.getId());
        String taskType = defaultString(request.taskType(), "CUSTOMER_FOLLOW_UP");
        UUID existingTaskId = existingTaskId(lead.getId(), mailboxEmail.getId(), taskType);
        if (existingTaskId != null) {
            return new MailboxBusinessDtos.TaskFromMailboxEmailResponse(true, mapper.task(entityTask(existingTaskId)), mapper.lead(lead), mapper.mailboxEmail(mailboxEmail));
        }
        String taskNo = "TASK-" + shortHash(lead.getId() + "|" + mailboxEmail.getId() + "|" + taskType);
        TaskDtos.TaskResponse task = storageService.upsertTask(new TaskDtos.TaskRequest(
                null,
                taskNo,
                lead.getId(),
                email.getId(),
                defaultString(request.assignedSalesEmail(), lead.getOwnerSalesEmail()),
                "PROPOSED",
                taskType,
                defaultString(request.priority(), "NORMAL"),
                defaultString(request.title(), fallbackTaskTitle(mailboxEmail)),
                defaultString(request.summary(), mailboxEmail.getSnippet()),
                defaultString(request.reason(), defaultString(request.summary(), mailboxEmail.getSnippet())),
                defaultString(request.suggestedAction(), defaultString(request.summary(), mailboxEmail.getSnippet())),
                defaultString(request.summary(), mailboxEmail.getSnippet()),
                defaultString(request.summary(), mailboxEmail.getSnippet()),
                defaultString(mailboxEmail.getSubject(), request.summary()),
                request.actionItems() == null ? List.of() : request.actionItems(),
                context(request.contextSnapshot(), mailboxEmail),
                request.priorityReason(),
                request.dueAt(),
                null,
                null,
                null,
                null,
                "SALES_LEADS_AGENT_V2_TASK_SKILL"
        ));
        insertTaskRelation(task.id(), mailboxEmail.getId(), lead.getId(), taskType);
        mailboxEmailService.markProcessed(mailboxEmail.getId(), "Follow-up task created from inbound mailbox email");
        return new MailboxBusinessDtos.TaskFromMailboxEmailResponse(false, task, mapper.lead(lead), mapper.mailboxEmail(mailboxEmail));
    }

    @Transactional
    public MailboxBusinessDtos.ActivityFromMailboxEmailResponse createActivity(MailboxBusinessDtos.ActivityFromMailboxEmailRequest request) {
        MailboxEmail mailboxEmail = mailboxEmailService.getEntity(required(request.mailboxEmailId(), "mailboxEmailId"));
        assertDirection(mailboxEmail, "OUTBOUND_SALES", "MAILBOX_EMAIL_NOT_OUTBOUND_SALES");
        EmailMessage email = mailboxEmailService.ensureEmailMessage(mailboxEmail.getId());
        LeadRecord lead = resolveLead(request.leadId(), request.customerEmail(), request.productBundleCode(), firstCustomerRecipient(mailboxEmail), email.getId());
        String activityType = defaultString(request.activityType(), "EMAIL_SENT");
        UUID existingActivityId = existingActivityId(lead.getId(), mailboxEmail.getId(), activityType);
        if (existingActivityId != null) {
            return new MailboxBusinessDtos.ActivityFromMailboxEmailResponse(true, mapper.salesActivity(entityActivity(existingActivityId)), mapper.lead(lead), mapper.email(email), null, mapper.mailboxEmail(mailboxEmail));
        }
        LeadEmail leadEmail = leadEmailRepository.findByLeadIdAndEmailMessageId(lead.getId(), email.getId()).orElseGet(LeadEmail::new);
        boolean newRelation = leadEmail.getId() == null;
        leadEmail.setId(leadEmail.getId() == null ? UUID.randomUUID() : leadEmail.getId());
        leadEmail.setLead(lead);
        leadEmail.setEmailMessage(email);
        leadEmail.setRelationType("SALES_REPLY");
        leadEmail.setMatchConfidence(request.confidence() == null ? BigDecimal.valueOf(0.9) : request.confidence());
        leadEmail.setMatchReason("Matched outbound mailbox email by lead context");
        if (newRelation) {
            leadEmail.setCreatedAt(Instant.now());
        }
        leadEmail = leadEmailRepository.save(leadEmail);
        Map<String, Object> extracted = request.extractedPayload() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(request.extractedPayload());
        extracted.put("mailboxEmailId", mailboxEmail.getId().toString());
        extracted.put("subject", mailboxEmail.getSubject());
        String activityNo = "ACT-" + shortHash(lead.getId() + "|" + mailboxEmail.getId() + "|" + activityType);
        SalesActivityDtos.SalesActivityResponse activity = storageService.upsertSalesActivity(new SalesActivityDtos.SalesActivityRequest(
                null,
                activityNo,
                lead.getId(),
                email.getId(),
                parseUuid(request.relatedTaskId()),
                mailboxEmail.getMailbox(),
                activityType,
                occurredAt(mailboxEmail),
                defaultString(request.title(), mailboxEmail.getSubject()),
                requiredString(defaultString(request.summary(), mailboxEmail.getSnippet()), "summary"),
                request.keyPoints() == null ? List.of() : request.keyPoints(),
                request.customerSignals() == null ? List.of() : request.customerSignals(),
                request.nextStepSignals() == null ? List.of() : request.nextStepSignals(),
                defaultString(request.progressSignal(), "PROGRESS"),
                defaultString(request.progressReason(), "销售已发送业务跟进邮件。"),
                defaultString(request.stageAfterActivity(), "Contacted"),
                extracted,
                request.confidence() == null ? BigDecimal.valueOf(0.9) : request.confidence(),
                "SALES_LEADS_AGENT_V2_ACTIVITY_SKILL"
        ));
        insertActivityRelation(activity.id(), mailboxEmail.getId(), lead.getId(), activityType);
        mailboxEmailService.markProcessed(mailboxEmail.getId(), "Sales activity created from outbound mailbox email");
        return new MailboxBusinessDtos.ActivityFromMailboxEmailResponse(false, activity, mapper.lead(lead), mapper.email(email), mapper.leadEmail(leadEmail), mapper.mailboxEmail(mailboxEmail));
    }

    private LeadRecord resolveLead(UUID leadId, String customerEmail, String productBundleCode, String fallbackCustomerEmail, UUID emailMessageId) {
        if (leadId != null) {
            return leadRepository.findById(leadId).orElseThrow(() -> new NotFoundException("Lead not found: " + leadId));
        }
        if (emailMessageId != null) {
            Optional<LeadEmail> linked = leadEmailRepository.findFirstByEmailMessageIdOrderByCreatedAtDesc(emailMessageId);
            if (linked.isPresent()) {
                return linked.get().getLead();
            }
        }
        String customer = defaultString(customerEmail, fallbackCustomerEmail).toLowerCase(Locale.ROOT);
        if (productBundleCode == null || productBundleCode.isBlank()) {
            List<LeadRecord> leads = leadRepository.findByCustomerEmailNormalizedOrderByUpdatedAtDesc(customer);
            if (leads.size() == 1) {
                return leads.get(0);
            }
        }
        String code = requiredString(productBundleCode, "productBundleCode");
        ProductBundle product = productBundleService.getEntityByCode(code);
        return leadRepository.findByCustomerEmailNormalizedAndProductBundleId(customer, product.getId())
                .orElseThrow(() -> new NotFoundException("No matching lead for customer + product: " + customer + " / " + code));
    }

    private FollowUpTask entityTask(UUID id) {
        return taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Task not found: " + id));
    }

    private SalesActivity entityActivity(UUID id) {
        return activityRepository.findById(id).orElseThrow(() -> new NotFoundException("Activity not found: " + id));
    }

    private UUID existingTaskId(UUID leadId, UUID mailboxEmailId, String taskType) {
        List<?> rows = entityManager.createNativeQuery(
                        "select follow_up_task_id from follow_up_task_mailbox_email where lead_id = :leadId and mailbox_email_id = :mailboxEmailId and task_type = :taskType")
                .setParameter("leadId", leadId)
                .setParameter("mailboxEmailId", mailboxEmailId)
                .setParameter("taskType", taskType)
                .getResultList();
        return rows.isEmpty() ? null : (UUID) rows.get(0);
    }

    private void insertTaskRelation(UUID taskId, UUID mailboxEmailId, UUID leadId, String taskType) {
        entityManager.createNativeQuery(
                        "insert into follow_up_task_mailbox_email(follow_up_task_id, mailbox_email_id, lead_id, task_type) values (:taskId, :mailboxEmailId, :leadId, :taskType) on conflict do nothing")
                .setParameter("taskId", taskId)
                .setParameter("mailboxEmailId", mailboxEmailId)
                .setParameter("leadId", leadId)
                .setParameter("taskType", taskType)
                .executeUpdate();
    }

    private UUID existingActivityId(UUID leadId, UUID mailboxEmailId, String activityType) {
        List<?> rows = entityManager.createNativeQuery(
                        "select sales_activity_id from sales_activity_mailbox_email where lead_id = :leadId and mailbox_email_id = :mailboxEmailId and activity_type = :activityType")
                .setParameter("leadId", leadId)
                .setParameter("mailboxEmailId", mailboxEmailId)
                .setParameter("activityType", activityType)
                .getResultList();
        return rows.isEmpty() ? null : (UUID) rows.get(0);
    }

    private void insertActivityRelation(UUID activityId, UUID mailboxEmailId, UUID leadId, String activityType) {
        entityManager.createNativeQuery(
                        "insert into sales_activity_mailbox_email(sales_activity_id, mailbox_email_id, lead_id, activity_type) values (:activityId, :mailboxEmailId, :leadId, :activityType) on conflict do nothing")
                .setParameter("activityId", activityId)
                .setParameter("mailboxEmailId", mailboxEmailId)
                .setParameter("leadId", leadId)
                .setParameter("activityType", activityType)
                .executeUpdate();
    }

    private static void assertDirection(MailboxEmail email, String expected, String code) {
        if (!expected.equalsIgnoreCase(email.getDirection())) {
            throw new BadRequestException(code + ": expected " + expected + ", got " + email.getDirection());
        }
    }

    private static UUID parseUuid(String value) {
        return value == null || value.isBlank() ? null : UUID.fromString(value);
    }

    private static Instant occurredAt(MailboxEmail email) {
        Instant value = email.getReceivedAt() != null ? email.getReceivedAt() : email.getSentAt();
        return value == null ? Instant.now() : value;
    }

    private static String fallbackTaskTitle(MailboxEmail email) {
        String subject = email.getSubject() == null ? "客户咨询" : email.getSubject();
        return "跟进客户邮件：" + subject;
    }

    private static String firstCustomerRecipient(MailboxEmail email) {
        for (String item : email.getToEmails()) {
            if (!item.equalsIgnoreCase(email.getMailbox())) {
                return item;
            }
        }
        return email.getToEmails().isEmpty() ? "" : email.getToEmails().get(0);
    }

    private static Map<String, Object> context(Map<String, Object> input, MailboxEmail email) {
        Map<String, Object> output = input == null ? new LinkedHashMap<>() : new LinkedHashMap<>(input);
        output.put("mailboxEmailId", email.getId().toString());
        output.put("subject", email.getSubject());
        output.put("snippet", email.getSnippet());
        return output;
    }

    private static UUID required(UUID value, String field) {
        if (value == null) {
            throw new BadRequestException("Missing required field: " + field);
        }
        return value;
    }

    private static String requiredString(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Missing required field: " + field);
        }
        return value.trim();
    }

    private static String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String shortHash(String value) {
        return UUID.nameUUIDFromBytes(value.getBytes()).toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
    }
}
