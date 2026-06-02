package com.genscript.leads.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ProductBundleDtos {
    private ProductBundleDtos() {
    }

    public record ProductBundleRequest(
            UUID id,
            String code,
            String pathEn,
            String pathCn,
            String businessUnit,
            String categoryLevel1,
            String categoryLevel2,
            String categoryLevel3,
            List<String> synonyms,
            Boolean active
    ) {
    }

    public record ProductBundleResponse(
            UUID id,
            String code,
            String pathEn,
            String pathCn,
            String businessUnit,
            String categoryLevel1,
            String categoryLevel2,
            String categoryLevel3,
            List<String> synonyms,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record InitializeProductBundlesResponse(
            int parsedLeafCategories,
            int inserted,
            int updated
    ) {
    }
}
