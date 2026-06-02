package com.genscript.leads.service;

import com.genscript.leads.domain.*;
import com.genscript.leads.dto.*;
import com.genscript.leads.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgentContextService {
    private static final Pattern TOKEN_SPLIT = Pattern.compile("[^a-z0-9\\u4e00-\\u9fff]+");

    private final ProductBundleRepository productBundleRepository;
    private final LeadRecordRepository leadRepository;
    private final EmailMessageRepository emailRepository;
    private final FollowUpTaskRepository taskRepository;
    private final SalesActivityRepository activityRepository;
    private final StorageService storageService;
    private final Mapper mapper;

    @Transactional(readOnly = true)
    public List<AgentContextDtos.ProductBundleCandidate> searchProductBundles(String query, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        return productBundleRepository.findAll().stream()
                .filter(ProductBundle::isActive)
                .map(bundle -> new ScoredBundle(bundle, productScore(query, bundle), productReason(query, bundle)))
                .sorted(Comparator.comparingDouble(ScoredBundle::score).reversed())
                .limit(safeLimit)
                .map(item -> new AgentContextDtos.ProductBundleCandidate(
                        mapper.productBundle(item.bundle()), item.score(), item.reason()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AgentContextDtos.LeadContextMatch> matchLeads(
            String customerEmail,
            String counterpartyEmail,
            String productBundleCode,
            String subject,
            int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        String targetEmail = normalize(firstNonBlank(customerEmail, counterpartyEmail));
        Set<String> subjectTokens = tokens(subject);
        return leadRepository.findAll().stream()
                .map(lead -> scoreLead(lead, targetEmail, productBundleCode, subjectTokens))
                .filter(item -> item.score() > 0)
                .sorted(Comparator.comparingDouble(ScoredLead::score).reversed())
                .limit(safeLimit)
                .map(item -> leadContext(item.lead(), item.score(), item.reason()))
                .toList();
    }

    @Transactional(readOnly = true)
    public EmailDtos.EmailResponse emailByProvider(String providerEmailId) {
        return emailRepository.findByProviderEmailId(providerEmailId)
                .map(mapper::email)
                .orElseThrow(() -> new NotFoundException("Email not found by providerEmailId: " + providerEmailId));
    }

    private AgentContextDtos.LeadContextMatch leadContext(LeadRecord lead, double score, String reason) {
        UUID leadId = lead.getId();
        List<TaskDtos.TaskResponse> openTasks = taskRepository.findByLeadIdOrderByCreatedAtDesc(leadId).stream()
                .filter(task -> !Set.of("DONE", "DISMISSED").contains(defaultString(task.getStatus()).toUpperCase(Locale.ROOT)))
                .limit(5)
                .map(mapper::task)
                .toList();
        List<SalesActivityDtos.SalesActivityResponse> recentActivities = activityRepository.findByLeadIdOrderByOccurredAtDesc(leadId).stream()
                .limit(5)
                .map(mapper::salesActivity)
                .toList();
        Map<String, Long> timelineCounts = storageService.timeline(leadId).items().stream()
                .collect(Collectors.groupingBy(TimelineDtos.TimelineItem::type, LinkedHashMap::new, Collectors.counting()));
        return new AgentContextDtos.LeadContextMatch(
                mapper.lead(lead), score, reason, openTasks, recentActivities, timelineCounts
        );
    }

    private ScoredLead scoreLead(LeadRecord lead, String targetEmail, String productBundleCode, Set<String> subjectTokens) {
        double score = 0;
        List<String> reasons = new ArrayList<>();
        ProductBundle product = lead.getProductBundle();
        if (!targetEmail.isBlank() && normalize(lead.getCustomerEmailNormalized()).equals(targetEmail)) {
            score += 50;
            reasons.add("same customer email");
        }
        if (hasText(productBundleCode) && product != null && productBundleCode.equals(product.getCode())) {
            score += 35;
            reasons.add("same product bundle");
        }
        Set<String> leadTokens = tokens(String.join(" ",
                defaultString(lead.getLeadNo()),
                defaultString(lead.getCustomerName()),
                defaultString(lead.getCustomerEmail()),
                defaultString(lead.getInquirySummary()),
                product == null ? "" : defaultString(product.getPathEn()),
                product == null ? "" : defaultString(product.getPathCn())
        ));
        Set<String> overlap = new LinkedHashSet<>(subjectTokens);
        overlap.retainAll(leadTokens);
        if (!overlap.isEmpty()) {
            score += overlap.size() * 3.0;
            reasons.add("subject/context overlap: " + String.join(", ", overlap.stream().limit(6).toList()));
        }
        return new ScoredLead(lead, score, reasons.isEmpty() ? "low-confidence context candidate" : String.join("; ", reasons));
    }

    private double productScore(String query, ProductBundle bundle) {
        Set<String> queryTokens = tokens(query);
        Set<String> hayTokens = tokens(productText(bundle));
        Set<String> overlap = new LinkedHashSet<>(queryTokens);
        overlap.retainAll(hayTokens);
        double phraseBonus = queryTokens.stream().filter(token -> normalize(productText(bundle)).contains(token)).count() * 2.0;
        return overlap.size() * 10.0 + phraseBonus;
    }

    private String productReason(String query, ProductBundle bundle) {
        Set<String> overlap = new LinkedHashSet<>(tokens(query));
        overlap.retainAll(tokens(productText(bundle)));
        if (!overlap.isEmpty()) {
            return "Matched query terms: " + String.join(", ", overlap.stream().limit(8).toList());
        }
        return "Best available product bundle candidate";
    }

    private String productText(ProductBundle bundle) {
        return String.join(" ",
                defaultString(bundle.getCode()),
                defaultString(bundle.getPathEn()),
                defaultString(bundle.getPathCn()),
                defaultString(bundle.getBusinessUnit()),
                defaultString(bundle.getCategoryLevel1()),
                defaultString(bundle.getCategoryLevel2()),
                defaultString(bundle.getCategoryLevel3()),
                String.join(" ", bundle.getSynonyms() == null ? List.of() : bundle.getSynonyms())
        );
    }

    private Set<String> tokens(String text) {
        return Arrays.stream(TOKEN_SPLIT.split(normalize(text)))
                .filter(token -> token.length() > 1)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalize(String value) {
        return defaultString(value).toLowerCase(Locale.ROOT).trim();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String firstNonBlank(String first, String second) {
        return hasText(first) ? first : defaultString(second);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private record ScoredBundle(ProductBundle bundle, double score, String reason) {
    }

    private record ScoredLead(LeadRecord lead, double score, String reason) {
    }
}
