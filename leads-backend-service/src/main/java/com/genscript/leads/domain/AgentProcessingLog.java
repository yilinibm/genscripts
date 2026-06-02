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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "agent_processing_log")
public class AgentProcessingLog {
    @Id
    private UUID id;

    @Column(nullable = false, length = 64)
    private String sourceType;

    @Column(nullable = false, length = 256)
    private String sourceId;

    @Column(nullable = false, length = 128)
    private String action;

    @Column(nullable = false, length = 64)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> requestPayload = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> resultPayload = new LinkedHashMap<>();

    @Column(columnDefinition = "text")
    private String errorMessage;

    private Instant startedAt;
    private Instant finishedAt;
}
