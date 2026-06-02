package com.genscript.leads.repository;

import com.genscript.leads.domain.LeadEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadEmailRepository extends JpaRepository<LeadEmail, UUID> {
    Optional<LeadEmail> findByLeadIdAndEmailMessageId(UUID leadId, UUID emailMessageId);
    Optional<LeadEmail> findFirstByEmailMessageIdOrderByCreatedAtDesc(UUID emailMessageId);
    List<LeadEmail> findByLeadIdOrderByCreatedAtDesc(UUID leadId);
    void deleteByLeadId(UUID leadId);
}
