package com.genscript.leads.service;

import com.genscript.leads.domain.*;
import com.genscript.leads.dto.*;
import org.springframework.stereotype.Component;

@Component
public class Mapper {
    public ProductBundleDtos.ProductBundleResponse productBundle(ProductBundle e) {
        return new ProductBundleDtos.ProductBundleResponse(
                e.getId(), e.getCode(), e.getPathEn(), e.getPathCn(), e.getBusinessUnit(),
                e.getCategoryLevel1(), e.getCategoryLevel2(), e.getCategoryLevel3(),
                e.getSynonyms(), e.isActive(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    public LeadDtos.LeadResponse lead(LeadRecord e) {
        return new LeadDtos.LeadResponse(
                e.getId(), e.getLeadNo(), e.getCustomerEmail(), e.getCustomerEmailNormalized(),
                e.getCustomerName(), e.getCompany(), e.getTitle(), e.getPhone(),
                productBundle(e.getProductBundle()), e.getLeadUniqueKey(), e.getOwnerSalesEmail(),
                e.getStatus(), e.getIntentLevel(), e.getCurrentStage(), e.getTimelineTrend(),
                e.getLatestTimelineSummaryId(), e.getSource(), e.getInquirySummary(),
                e.getExtractedRequirements(), e.getFirstEmailId(), e.getLatestEmailAt(),
                e.getLastCustomerEmailAt(), e.getLastSalesActivityAt(), e.getCreatedBy(),
                e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    public EmailDtos.EmailResponse email(EmailMessage e) {
        return new EmailDtos.EmailResponse(
                e.getId(), e.getProviderEmailId(), e.getMailbox(), e.getThreadId(), e.getDirection(),
                e.getFromEmail(), e.getFromName(), e.getToEmails(), e.getCcEmails(), e.getSubject(),
                e.getBodyText(), e.getBodyHtmlRef(), e.getSnippet(), e.getAttachmentRefs(),
                e.getSentAt(), e.getReceivedAt(), e.getProcessedAt(), e.getEmailStatus(),
                e.getRawStorageRef(), e.getCreatedAt()
        );
    }

    public MailboxEmailDtos.MailboxEmailResponse mailboxEmail(MailboxEmail e) {
        return new MailboxEmailDtos.MailboxEmailResponse(
                e.getId(), e.getProvider(), e.getMailbox(), e.getFolder(), e.getProviderUid(),
                e.getMessageId(), e.getDedupeKey(), e.getDirection(), e.getFromEmail(), e.getFromName(),
                e.getToEmails(), e.getCcEmails(), e.getSubject(), e.getBodyText(), e.getSnippet(),
                e.getSentAt(), e.getReceivedAt(), e.getSyncedAt(), e.getProcessingStatus(),
                e.getProcessedAt(), e.getProcessingReason(),
                e.getEmailMessage() == null ? null : e.getEmailMessage().getId(),
                e.getRawPayload(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    public LeadEmailDtos.LeadEmailResponse leadEmail(LeadEmail e) {
        return new LeadEmailDtos.LeadEmailResponse(
                e.getId(), e.getLead().getId(), e.getEmailMessage().getId(), e.getRelationType(),
                e.getMatchConfidence(), e.getMatchReason(), e.getCreatedAt()
        );
    }

    public TaskDtos.TaskResponse task(FollowUpTask e) {
        return new TaskDtos.TaskResponse(
                e.getId(), e.getTaskNo(), e.getLead().getId(),
                e.getSourceEmail() == null ? null : e.getSourceEmail().getId(),
                e.getAssignedSalesEmail(), e.getStatus(), e.getTaskType(), e.getPriority(), e.getTitle(),
                e.getSummary(), e.getReason(), e.getSuggestedAction(), e.getDisplaySummary(),
                e.getCustomerNeedSummary(), e.getSourceEventSummary(), e.getActionItems(),
                e.getContextSnapshot(), e.getPriorityReason(), e.getDueAt(), e.getAcceptedAt(),
                e.getCompletedAt(), e.getDismissedAt(), e.getCloseReason(), e.getCreatedBy(),
                e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    public SalesActivityDtos.SalesActivityResponse salesActivity(SalesActivity e) {
        return new SalesActivityDtos.SalesActivityResponse(
                e.getId(), e.getActivityNo(), e.getLead().getId(),
                e.getSourceEmail() == null ? null : e.getSourceEmail().getId(),
                e.getRelatedTask() == null ? null : e.getRelatedTask().getId(),
                e.getSalesEmail(), e.getActivityType(), e.getOccurredAt(), e.getTitle(), e.getSummary(),
                e.getKeyPoints(), e.getCustomerSignals(), e.getNextStepSignals(), e.getProgressSignal(),
                e.getProgressReason(), e.getStageAfterActivity(), e.getExtractedPayload(), e.getConfidence(),
                e.getCreatedBy(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    public LeadActivitySummaryDtos.LeadActivitySummaryResponse leadActivitySummary(LeadActivitySummary e) {
        return new LeadActivitySummaryDtos.LeadActivitySummaryResponse(
                e.getId(), e.getLead().getId(), e.getSummaryWindow(), e.getWindowStartAt(), e.getWindowEndAt(),
                e.getOverallSummary(), e.getCustomerIntent(), e.getCurrentStage(), e.getTrend(), e.getTrendReason(),
                e.getProgressActivityCount(), e.getNoProgressActivityCount(), e.getLastProgressAt(),
                e.getLastActivityAt(), e.getNextRecommendedAction(), e.getSourceActivityIds(),
                e.getConfidence(), e.getGeneratedBy(), e.getGeneratedAt()
        );
    }
}
