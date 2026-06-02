package com.genscript.leads.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final EntityManager entityManager;

    @Transactional
    public Map<String, Object> cleanup() {
        entityManager.createNativeQuery("truncate table agent_processing_log, sales_activity_mailbox_email, follow_up_task_mailbox_email, mailbox_email, lead_activity_summary, sales_activity, follow_up_task, lead_email, email_message, lead_record, product_bundle restart identity cascade").executeUpdate();
        return Map.of("status", "success", "message", "All leads backend data cleared");
    }
}
