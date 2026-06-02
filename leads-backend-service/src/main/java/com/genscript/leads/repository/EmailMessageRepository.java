package com.genscript.leads.repository;

import com.genscript.leads.domain.EmailMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailMessageRepository extends JpaRepository<EmailMessage, UUID> {
    Optional<EmailMessage> findByProviderEmailId(String providerEmailId);
    List<EmailMessage> findByThreadIdOrderByCreatedAtAsc(String threadId);
}
