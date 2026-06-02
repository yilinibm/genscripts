package com.genscript.leads.controller;

import com.genscript.leads.dto.ProductBundleDtos;
import com.genscript.leads.service.ProductBundleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/product-bundles")
@RequiredArgsConstructor
public class ProductBundleController {
    private final ProductBundleService service;

    @PostMapping
    public ProductBundleDtos.ProductBundleResponse upsert(@RequestBody ProductBundleDtos.ProductBundleRequest request) {
        return service.upsert(request);
    }

    @GetMapping
    public Page<ProductBundleDtos.ProductBundleResponse> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public ProductBundleDtos.ProductBundleResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping("/by-code/{code}")
    public ProductBundleDtos.ProductBundleResponse getByCode(@PathVariable String code) {
        return service.getByCode(code);
    }
}
