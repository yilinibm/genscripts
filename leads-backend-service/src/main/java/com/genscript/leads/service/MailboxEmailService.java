package com.genscript.leads.service;

import com.genscript.leads.domain.EmailMessage;
import com.genscript.leads.domain.MailboxEmail;
import com.genscript.leads.dto.EmailDtos;
import com.genscript.leads.dto.MailboxEmailDtos;
import com.genscript.leads.repository.EmailMessageRepository;
import com.genscript.leads.repository.MailboxEmailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MailboxEmailService {
    private static final Pattern SPACE = Pattern.compile("\\s+");

    private final MailboxEmailRepository mailboxEmailRepository;
    private final EmailMessageRepository emailMessageRepository;
    private final StorageService storageService;
    private final Mapper mapper;

    @Transactional
    public MailboxEmailDtos.MailboxEmailBatchResponse upsertBatch(MailboxEmailDtos.MailboxEmailBatchRequest request) {
        List<MailboxEmailDtos.MailboxEmailRequest> input = request == null || request.emails() == null ? List.of() : request.emails();
        List<MailboxEmailDtos.MailboxEmailResponse> output = new ArrayList<>();
        for (MailboxEmailDtos.MailboxEmailRequest item : input) {
            output.add(upsert(item));
        }
        return new MailboxEmailDtos.MailboxEmailBatchResponse(input.size(), output.size(), output);
    }

    @Transactional
    public MailboxEmailDtos.MailboxEmailResponse upsert(MailboxEmailDtos.MailboxEmailRequest request) {
        String dedupeKey = dedupeKey(request);
        MailboxEmail entity = mailboxEmailRepository.findByDedupeKey(dedupeKey).orElseGet(MailboxEmail::new);
        boolean isNew = entity.getId() == null;
        entity.setId(entity.getId() == null ? Objects.requireNonNullElseGet(request.id(), UUID::randomUUID) : entity.getId());
        entity.setProvider(defaultString(request.provider(), "163").toLowerCase(Locale.ROOT));
        entity.setMailbox(required(request.mailbox(), "mailbox").toLowerCase(Locale.ROOT));
        entity.setFolder(defaultString(request.folder(), "INBOX"));
        entity.setProviderUid(blankToNull(request.providerUid()));
        entity.setMessageId(blankToNull(request.messageId()));
        entity.setDedupeKey(dedupeKey);
        entity.setDirection(required(request.direction(), "direction").toUpperCase(Locale.ROOT));
        entity.setFromEmail(required(request.fromEmail(), "fromEmail").toLowerCase(Locale.ROOT));
        entity.setFromName(request.fromName());
        entity.setToEmails(normalizeEmails(request.toEmails()));
        entity.setCcEmails(normalizeEmails(request.ccEmails()));
        entity.setSubject(request.subject());
        entity.setBodyText(request.bodyText());
        entity.setSnippet(defaultString(request.snippet(), snippet(request.bodyText())));
        entity.setSentAt(request.sentAt());
        entity.setReceivedAt(request.receivedAt());
        entity.setSyncedAt(Instant.now());
        if (isNew) {
            entity.setProcessingStatus(defaultString(request.processingStatus(), "PENDING").toUpperCase(Locale.ROOT));
            entity.setCreatedAt(Instant.now());
        } else if (request.processingStatus() != null && !"PROCESSED".equalsIgnoreCase(entity.getProcessingStatus())) {
            entity.setProcessingStatus(request.processingStatus().toUpperCase(Locale.ROOT));
        }
        entity.setProcessingReason(request.processingReason());
        entity.setRawPayload(request.rawPayload() == null ? new LinkedHashMap<>() : request.rawPayload());
        entity.setUpdatedAt(Instant.now());
        return mapper.mailboxEmail(mailboxEmailRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<MailboxEmailDtos.MailboxEmailResponse> pending(String direction, String mailbox, Pageable pageable) {
        String normalizedDirection = blankToNull(direction);
        String normalizedMailbox = blankToNull(mailbox);
        Page<MailboxEmail> page;
        if (normalizedDirection != null && normalizedMailbox != null) {
            page = mailboxEmailRepository.findByMailboxAndDirectionAndProcessingStatus(
                    normalizedMailbox.toLowerCase(Locale.ROOT), normalizedDirection.toUpperCase(Locale.ROOT), "PENDING", pageable);
        } else if (normalizedDirection != null) {
            page = mailboxEmailRepository.findByDirectionAndProcessingStatus(normalizedDirection.toUpperCase(Locale.ROOT), "PENDING", pageable);
        } else if (normalizedMailbox != null) {
            page = mailboxEmailRepository.findByMailboxAndProcessingStatus(normalizedMailbox.toLowerCase(Locale.ROOT), "PENDING", pageable);
        } else {
            page = mailboxEmailRepository.findByProcessingStatus("PENDING", pageable);
        }
        return page.map(mapper::mailboxEmail);
    }

    @Transactional(readOnly = true)
    public Page<MailboxEmailDtos.MailboxEmailResponse> list(Pageable pageable) {
        return mailboxEmailRepository.findAll(pageable).map(mapper::mailboxEmail);
    }

    @Transactional(readOnly = true)
    public MailboxEmailDtos.MailboxEmailResponse get(UUID id) {
        return mapper.mailboxEmail(getEntity(id));
    }

    @Transactional
    public MailboxEmailDtos.MailboxEmailResponse markProcessed(UUID id, String reason) {
        MailboxEmail entity = getEntity(id);
        entity.setProcessingStatus("PROCESSED");
        entity.setProcessedAt(Instant.now());
        entity.setProcessingReason(reason);
        entity.setUpdatedAt(Instant.now());
        return mapper.mailboxEmail(mailboxEmailRepository.save(entity));
    }

    @Transactional
    public MailboxEmailDtos.MailboxEmailResponse markIgnored(UUID id, String reason) {
        MailboxEmail entity = getEntity(id);
        entity.setProcessingStatus("IGNORED");
        entity.setProcessedAt(Instant.now());
        entity.setProcessingReason(reason);
        entity.setUpdatedAt(Instant.now());
        return mapper.mailboxEmail(mailboxEmailRepository.save(entity));
    }

    @Transactional
    public EmailMessage ensureEmailMessage(UUID mailboxEmailId) {
        MailboxEmail mailboxEmail = getEntity(mailboxEmailId);
        if (mailboxEmail.getEmailMessage() != null) {
            return mailboxEmail.getEmailMessage();
        }
        String providerEmailId = "MAILBOX-" + mailboxEmail.getId();
        Optional<EmailMessage> existing = emailMessageRepository.findByProviderEmailId(providerEmailId);
        EmailMessage email;
        if (existing.isPresent()) {
            email = existing.get();
        } else {
            EmailDtos.EmailResponse response = storageService.upsertEmail(new EmailDtos.EmailRequest(
                    null,
                    providerEmailId,
                    mailboxEmail.getMailbox(),
                    mailboxEmail.getMessageId() == null ? mailboxEmail.getDedupeKey() : mailboxEmail.getMessageId(),
                    mailboxEmail.getDirection(),
                    mailboxEmail.getFromEmail(),
                    mailboxEmail.getFromName(),
                    mailboxEmail.getToEmails(),
                    mailboxEmail.getCcEmails(),
                    mailboxEmail.getSubject(),
                    mailboxEmail.getBodyText(),
                    null,
                    mailboxEmail.getSnippet(),
                    List.of(),
                    mailboxEmail.getSentAt(),
                    mailboxEmail.getReceivedAt(),
                    Instant.now(),
                    "PROCESSED",
                    "mailbox_email:" + mailboxEmail.getId()
            ));
            email = emailMessageRepository.findById(response.id()).orElseThrow();
        }
        mailboxEmail.setEmailMessage(email);
        mailboxEmail.setUpdatedAt(Instant.now());
        mailboxEmailRepository.save(mailboxEmail);
        return email;
    }

    public MailboxEmail getEntity(UUID id) {
        return mailboxEmailRepository.findById(id).orElseThrow(() -> new NotFoundException("Mailbox email not found: " + id));
    }

    private String dedupeKey(MailboxEmailDtos.MailboxEmailRequest request) {
        String provider = defaultString(request.provider(), "163").toLowerCase(Locale.ROOT);
        String mailbox = required(request.mailbox(), "mailbox").toLowerCase(Locale.ROOT);
        String folder = defaultString(request.folder(), "INBOX").toLowerCase(Locale.ROOT);
        if (request.messageId() != null && !request.messageId().isBlank()) {
            return provider + ":message-id:" + normalize(request.messageId());
        }
        if (request.providerUid() != null && !request.providerUid().isBlank()) {
            return provider + ":" + mailbox + ":" + folder + ":uid:" + normalize(request.providerUid());
        }
        Instant occurredAt = request.receivedAt() != null ? request.receivedAt() : request.sentAt();
        String minute = occurredAt == null ? "" : occurredAt.truncatedTo(ChronoUnit.MINUTES).toString();
        String bodyHash = sha256(normalize(request.bodyText()));
        return provider + ":fingerprint:" + sha256(String.join("|",
                mailbox,
                folder,
                defaultString(request.direction(), "").toUpperCase(Locale.ROOT),
                normalize(request.fromEmail()),
                String.join(",", normalizeEmails(request.toEmails())),
                normalizeSubject(request.subject()),
                minute,
                bodyHash
        ));
    }

    private static List<String> normalizeEmails(List<String> values) {
        if (values == null) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String value : values) {
            String text = normalize(value);
            if (text.contains("<") && text.contains(">")) {
                int start = text.indexOf('<');
                int end = text.indexOf('>', start + 1);
                if (end > start) {
                    text = text.substring(start + 1, end).trim();
                }
            }
            if (!text.isBlank() && !result.contains(text)) {
                result.add(text);
            }
        }
        return result;
    }

    private static String normalizeSubject(String subject) {
        String text = normalize(subject);
        text = text.replaceFirst("^(re|fw|fwd)\\s*[:：]\\s*", "");
        text = text.replaceFirst("^\\[[^]]+]\\s*", "");
        return text;
    }

    private static String snippet(String body) {
        String text = SPACE.matcher(body == null ? "" : body).replaceAll(" ").trim();
        return text.length() <= 240 ? text : text.substring(0, 240);
    }

    private static String normalize(String value) {
        return SPACE.matcher(value == null ? "" : value).replaceAll(" ").trim().toLowerCase(Locale.ROOT);
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception exc) {
            throw new IllegalStateException("SHA-256 unavailable", exc);
        }
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Missing required field: " + field);
        }
        return value.trim();
    }

    private static String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
