package com.genscript.leads.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "lead_record")
public class LeadRecord {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String leadNo;

    @Column(nullable = false, length = 320)
    private String customerEmail;

    @Column(nullable = false, length = 320)
    private String customerEmailNormalized;

    private String customerName;
    private String company;
    private String title;
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_bundle_id")
    private ProductBundle productBundle;

    @Column(nullable = false, length = 1024)
    private String leadUniqueKey;

    @Column(nullable = false, length = 320)
    private String ownerSalesEmail;

    @Column(nullable = false, length = 64)
    private String status;

    private String intentLevel;
    private String currentStage;
    private String timelineTrend;

    @Column(name = "latest_timeline_summary_id")
    private UUID latestTimelineSummaryId;

    @Column(nullable = false, length = 64)
    private String source = "EMAIL";

    @Column(columnDefinition = "text")
    private String inquirySummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> extractedRequirements = new LinkedHashMap<>();

    private UUID firstEmailId;
    private Instant latestEmailAt;
    private Instant lastCustomerEmailAt;
    private Instant lastSalesActivityAt;

    @Column(nullable = false, length = 64)
    private String createdBy = "LEADS_AGENT";

    private Instant createdAt;
    private Instant updatedAt;

    @Version
    private Long version;
}
