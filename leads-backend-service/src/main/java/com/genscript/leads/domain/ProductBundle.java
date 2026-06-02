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
@Table(name = "product_bundle")
public class ProductBundle {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 128)
    private String code;

    @Column(nullable = false, length = 512)
    private String pathEn;

    @Column(nullable = false, length = 512)
    private String pathCn;

    @Column(nullable = false, length = 64)
    private String businessUnit;

    private String categoryLevel1;
    private String categoryLevel2;
    private String categoryLevel3;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> synonyms = new ArrayList<>();

    private boolean active = true;
    private Instant createdAt;
    private Instant updatedAt;
}
