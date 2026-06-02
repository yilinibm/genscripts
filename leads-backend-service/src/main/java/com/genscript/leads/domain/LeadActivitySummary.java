package com.genscript.leads.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "lead_activity_summary")
public class LeadActivitySummary {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lead_id")
    private LeadRecord lead;

    @Column(nullable = false, length = 32)
    private String summaryWindow;

    @Column(nullable = false)
    private Instant windowStartAt;

    @Column(nullable = false)
    private Instant windowEndAt;

    @Column(nullable = false, columnDefinition = "text")
    private String overallSummary;

    @Column(nullable = false, length = 64)
    private String customerIntent = "UNCLEAR";

    @Column(nullable = false, length = 64)
    private String currentStage;

    @Column(nullable = false, length = 64)
    private String trend;

    @Column(columnDefinition = "text")
    private String trendReason;

    private Integer progressActivityCount = 0;
    private Integer noProgressActivityCount = 0;
    private Instant lastProgressAt;
    private Instant lastActivityAt;

    @Column(columnDefinition = "text")
    private String nextRecommendedAction;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<UUID> sourceActivityIds = new ArrayList<>();

    private BigDecimal confidence;

    @Column(nullable = false, length = 64)
    private String generatedBy = "LEADS_AGENT";

    private Instant generatedAt;
}
