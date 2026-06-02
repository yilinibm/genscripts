package com.genscript.leads.repository;

import com.genscript.leads.domain.LeadActivitySummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface LeadActivitySummaryRepository extends JpaRepository<LeadActivitySummary, UUID> {
    Optional<LeadActivitySummary> findByLeadIdAndSummaryWindowAndWindowEndAt(
            UUID leadId,
            String summaryWindow,
            Instant windowEndAt
    );

    Optional<LeadActivitySummary> findFirstByLeadIdAndSummaryWindowOrderByGeneratedAtDesc(
            UUID leadId,
            String summaryWindow
    );
}
