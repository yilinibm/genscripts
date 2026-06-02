package com.genscript.leads.repository;

import com.genscript.leads.domain.MailboxEmail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MailboxEmailRepository extends JpaRepository<MailboxEmail, UUID> {
    Optional<MailboxEmail> findByDedupeKey(String dedupeKey);
    Page<MailboxEmail> findByMailboxAndDirectionAndProcessingStatus(String mailbox, String direction, String processingStatus, Pageable pageable);
    Page<MailboxEmail> findByDirectionAndProcessingStatus(String direction, String processingStatus, Pageable pageable);
    Page<MailboxEmail> findByMailboxAndProcessingStatus(String mailbox, String processingStatus, Pageable pageable);
    Page<MailboxEmail> findByProcessingStatus(String processingStatus, Pageable pageable);
}
