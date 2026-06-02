package com.genscript.leads.service;

import com.genscript.leads.domain.ProductBundle;
import com.genscript.leads.dto.ProductBundleDtos;
import com.genscript.leads.repository.ProductBundleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductBundleService {
    private static final Pattern HEADING = Pattern.compile("^(#{3,6})\\s+(.+?)（(.+?)）\\s*$");
    private static final Pattern BULLET = Pattern.compile("^(\\s*)-\\s+(.+?)（(.+?)）\\s*$");

    private final ProductBundleRepository repository;
    private final Mapper mapper;

    @Value("${leads.product-categories-path}")
    private String productCategoriesPath;

    public Page<ProductBundleDtos.ProductBundleResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::productBundle);
    }

    public ProductBundle getEntity(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Product bundle not found: " + id));
    }

    public ProductBundle getEntityByCode(String code) {
        return repository.findByCode(code).orElseThrow(() -> new NotFoundException("Product bundle not found: " + code));
    }

    public ProductBundleDtos.ProductBundleResponse get(UUID id) {
        return mapper.productBundle(getEntity(id));
    }

    public ProductBundleDtos.ProductBundleResponse getByCode(String code) {
        return mapper.productBundle(getEntityByCode(code));
    }

    @Transactional
    public ProductBundleDtos.ProductBundleResponse upsert(ProductBundleDtos.ProductBundleRequest request) {
        ProductBundle entity = findExisting(request).orElseGet(ProductBundle::new);
        boolean isNew = entity.getId() == null;
        apply(entity, request);
        if (isNew) {
            entity.setCreatedAt(Instant.now());
        }
        entity.setUpdatedAt(Instant.now());
        return mapper.productBundle(repository.save(entity));
    }

    @Transactional
    public ProductBundleDtos.InitializeProductBundlesResponse initializeFromMarkdown() {
        List<CategoryPath> paths = parseCategoryPaths();
        int inserted = 0;
        int updated = 0;
        for (CategoryPath path : paths) {
            String code = stableCode(path.pathEn());
            ProductBundle entity = repository.findByCode(code).orElseGet(ProductBundle::new);
            boolean isNew = entity.getId() == null;
            entity.setId(isNew ? UUID.randomUUID() : entity.getId());
            entity.setCode(code);
            entity.setPathEn(path.pathEn());
            entity.setPathCn(path.pathCn());
            entity.setBusinessUnit(path.businessUnit());
            entity.setCategoryLevel1(path.level(1));
            entity.setCategoryLevel2(path.level(2));
            entity.setCategoryLevel3(path.level(3));
            entity.setSynonyms(path.synonyms());
            entity.setActive(true);
            if (isNew) {
                entity.setCreatedAt(Instant.now());
                inserted++;
            } else {
                updated++;
            }
            entity.setUpdatedAt(Instant.now());
            repository.save(entity);
        }
        return new ProductBundleDtos.InitializeProductBundlesResponse(paths.size(), inserted, updated);
    }

    private Optional<ProductBundle> findExisting(ProductBundleDtos.ProductBundleRequest request) {
        if (request.id() != null) {
            return repository.findById(request.id());
        }
        if (request.code() != null && !request.code().isBlank()) {
            return repository.findByCode(request.code().trim());
        }
        return Optional.empty();
    }

    private void apply(ProductBundle entity, ProductBundleDtos.ProductBundleRequest request) {
        entity.setId(entity.getId() == null ? Objects.requireNonNullElseGet(request.id(), UUID::randomUUID) : entity.getId());
        entity.setCode(required(request.code(), "code"));
        entity.setPathEn(required(request.pathEn(), "pathEn"));
        entity.setPathCn(required(request.pathCn(), "pathCn"));
        entity.setBusinessUnit(required(request.businessUnit(), "businessUnit"));
        entity.setCategoryLevel1(request.categoryLevel1());
        entity.setCategoryLevel2(request.categoryLevel2());
        entity.setCategoryLevel3(request.categoryLevel3());
        entity.setSynonyms(request.synonyms() == null ? new ArrayList<>() : request.synonyms());
        entity.setActive(request.active() == null || request.active());
    }

    private List<CategoryPath> parseCategoryPaths() {
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(productCategoriesPath));
        } catch (IOException e) {
            throw new BadRequestException("Failed to read product categories markdown: " + productCategoriesPath);
        }

        TreeMap<Integer, LabelPair> stack = new TreeMap<>();
        List<CategoryPath> all = new ArrayList<>();
        for (String line : lines) {
            Matcher heading = HEADING.matcher(line);
            if (heading.matches()) {
                int level = heading.group(1).length() - 2;
                trimStack(stack, level);
                stack.put(level, new LabelPair(heading.group(2).trim(), heading.group(3).trim()));
                continue;
            }
            Matcher bullet = BULLET.matcher(line);
            if (bullet.matches()) {
                int level = 4 + bullet.group(1).length() / 2;
                trimStack(stack, level);
                stack.put(level, new LabelPair(bullet.group(2).trim(), bullet.group(3).trim()));
                all.add(toPath(stack.values()));
            }
        }

        Set<String> parentPaths = all.stream()
                .flatMap(path -> prefixes(path.pathEn()).stream())
                .collect(Collectors.toSet());
        return all.stream()
                .filter(path -> !parentPaths.contains(path.pathEn()))
                .distinct()
                .toList();
    }

    private static void trimStack(TreeMap<Integer, LabelPair> stack, int level) {
        stack.tailMap(level, true).clear();
    }

    private static CategoryPath toPath(Collection<LabelPair> labels) {
        List<LabelPair> list = labels.stream().toList();
        String en = list.stream().map(LabelPair::en).collect(Collectors.joining(" > "));
        String cn = list.stream().map(LabelPair::cn).collect(Collectors.joining(" > "));
        return new CategoryPath(en, cn, list);
    }

    private static List<String> prefixes(String path) {
        String[] parts = path.split(" > ");
        List<String> prefixes = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            prefixes.add(String.join(" > ", Arrays.copyOfRange(parts, 0, i)));
        }
        return prefixes;
    }

    private static String stableCode(String pathEn) {
        String normalized = Normalizer.normalize(pathEn, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return normalized.length() <= 128 ? normalized : normalized.substring(0, 128).replaceAll("_+$", "");
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Missing required field: " + field);
        }
        return value.trim();
    }

    private record LabelPair(String en, String cn) {
    }

    private record CategoryPath(String pathEn, String pathCn, List<LabelPair> levels) {
        String businessUnit() {
            return levels.isEmpty() ? "UNKNOWN" : levels.get(0).en();
        }

        String level(int index) {
            return levels.size() > index ? levels.get(index).en() : null;
        }

        List<String> synonyms() {
            if (levels.isEmpty()) {
                return List.of();
            }
            LabelPair leaf = levels.get(levels.size() - 1);
            return List.of(leaf.en(), leaf.cn());
        }
    }
}
