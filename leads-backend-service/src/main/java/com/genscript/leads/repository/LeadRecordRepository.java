package com.genscript.leads.repository;

import com.genscript.leads.domain.LeadRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadRecordRepository extends JpaRepository<LeadRecord, UUID> {
    Optional<LeadRecord> findByLeadNo(String leadNo);
    Optional<LeadRecord> findByCustomerEmailNormalizedAndProductBundleId(String email, UUID productBundleId);
    List<LeadRecord> findByCustomerEmailNormalizedOrderByUpdatedAtDesc(String email);
}
