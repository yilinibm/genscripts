package com.genscript.leads.repository;

import com.genscript.leads.domain.SalesActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalesActivityRepository extends JpaRepository<SalesActivity, UUID> {
    Optional<SalesActivity> findByActivityNo(String activityNo);
    List<SalesActivity> findByLeadIdOrderByOccurredAtDesc(UUID leadId);
}
