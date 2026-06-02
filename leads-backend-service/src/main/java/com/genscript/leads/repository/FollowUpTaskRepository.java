package com.genscript.leads.repository;

import com.genscript.leads.domain.FollowUpTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowUpTaskRepository extends JpaRepository<FollowUpTask, UUID> {
    Optional<FollowUpTask> findByTaskNo(String taskNo);
    Page<FollowUpTask> findByAssignedSalesEmailAndStatus(String assignedSalesEmail, String status, Pageable pageable);
    Page<FollowUpTask> findByLeadId(UUID leadId, Pageable pageable);
    List<FollowUpTask> findByLeadIdOrderByCreatedAtDesc(UUID leadId);
}
