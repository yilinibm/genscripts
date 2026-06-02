package com.genscript.leads.repository;

import com.genscript.leads.domain.AgentProcessingLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AgentProcessingLogRepository extends JpaRepository<AgentProcessingLog, UUID> {
    Optional<AgentProcessingLog> findBySourceTypeAndSourceIdAndAction(String sourceType, String sourceId, String action);
}
