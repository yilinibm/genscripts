package com.genscript.leads.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "follow_up_task")
public class FollowUpTask {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String taskNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lead_id")
    private LeadRecord lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_email_id")
    private EmailMessage sourceEmail;

    @Column(nullable = false, length = 320)
    private String assignedSalesEmail;

    @Column(nullable = false, length = 64)
    private String status;

    @Column(nullable = false, length = 64)
    private String taskType;

    @Column(nullable = false, length = 32)
    private String priority = "NORMAL";

    @Column(nullable = false, length = 512)
    private String title;

    @Column(columnDefinition = "text")
    private String summary;

    @Column(columnDefinition = "text")
    private String reason;

    @Column(columnDefinition = "text")
    private String suggestedAction;

    private String displaySummary;

    @Column(columnDefinition = "text")
    private String customerNeedSummary;

    @Column(columnDefinition = "text")
    private String sourceEventSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> actionItems = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> contextSnapshot = new LinkedHashMap<>();

    @Column(columnDefinition = "text")
    private String priorityReason;

    private Instant dueAt;
    private Instant acceptedAt;
    private Instant completedAt;
    private Instant dismissedAt;

    @Column(columnDefinition = "text")
    private String closeReason;

    @Column(nullable = false, length = 64)
    private String createdBy = "LEADS_AGENT";

    private Instant createdAt;
    private Instant updatedAt;

    @Version
    private Long version;
}
