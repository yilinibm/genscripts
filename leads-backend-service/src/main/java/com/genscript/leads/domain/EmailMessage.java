package com.genscript.leads.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "email_message")
public class EmailMessage {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 256)
    private String providerEmailId;

    @Column(nullable = false, length = 320)
    private String mailbox;

    private String threadId;

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
    private String bodyHtmlRef;

    @Column(columnDefinition = "text")
    private String snippet;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<Object> attachmentRefs = new ArrayList<>();

    private Instant sentAt;
    private Instant receivedAt;
    private Instant processedAt;
    private String emailStatus;

    @Column(columnDefinition = "text")
    private String rawStorageRef;

    private Instant createdAt;
}
