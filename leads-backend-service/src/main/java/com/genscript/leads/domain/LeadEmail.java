package com.genscript.leads.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "lead_email")
public class LeadEmail {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lead_id")
    private LeadRecord lead;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "email_message_id")
    private EmailMessage emailMessage;

    @Column(nullable = false, length = 64)
    private String relationType;

    private BigDecimal matchConfidence;

    @Column(columnDefinition = "text")
    private String matchReason;

    private Instant createdAt;
}
