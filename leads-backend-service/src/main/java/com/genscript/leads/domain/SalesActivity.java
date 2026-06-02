package com.genscript.leads.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "sales_activity")
public class SalesActivity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String activityNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lead_id")
    private LeadRecord lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_email_id")
    private EmailMessage sourceEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_task_id")
    private FollowUpTask relatedTask;

    @Column(nullable = false, length = 320)
    private String salesEmail;

    @Column(nullable = false, length = 64)
    private String activityType;

    @Column(nullable = false)
    private Instant occurredAt;

    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String summary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> keyPoints = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> customerSignals = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> nextStepSignals = new ArrayList<>();

    @Column(nullable = false, length = 32)
    private String progressSignal = "UNKNOWN";

    @Column(columnDefinition = "text")
    private String progressReason;

    private String stageAfterActivity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> extractedPayload = new LinkedHashMap<>();

    private BigDecimal confidence;

    @Column(nullable = false, length = 64)
    private String createdBy = "LEADS_AGENT";

    private Instant createdAt;
    private Instant updatedAt;
}
