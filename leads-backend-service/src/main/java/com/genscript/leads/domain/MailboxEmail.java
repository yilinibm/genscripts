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
@Table(name = "mailbox_email")
public class MailboxEmail {
    @Id
    private UUID id;

    @Column(nullable = false, length = 64)
    private String provider;

    @Column(nullable = false, length = 320)
    private String mailbox;

    @Column(nullable = false, length = 128)
    private String folder;

    private String providerUid;
    private String messageId;

    @Column(nullable = false, unique = true, length = 1024)
    private String dedupeKey;

    @Column(nullable = false, length = 32)
    private String direction;

    @Column(nullable = false, length = 320)
    private String fromEmail;

    private String fromName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> toEmails = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> ccEmails = new ArrayList<>();

    @Column(columnDefinition = "text")
    private String subject;

    @Column(columnDefinition = "text")
    private String bodyText;

    @Column(columnDefinition = "text")
    private String snippet;

    private Instant sentAt;
    private Instant receivedAt;
    private Instant syncedAt;

    @Column(nullable = false, length = 64)
    private String processingStatus = "PENDING";

    private Instant processedAt;

    @Column(columnDefinition = "text")
    private String processingReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_message_id")
    private EmailMessage emailMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> rawPayload = new LinkedHashMap<>();

    private Instant createdAt;
    private Instant updatedAt;
}
