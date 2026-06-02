package com.genscript.leads.controller;

import com.genscript.leads.dto.ProductBundleDtos;
import com.genscript.leads.service.AdminService;
import com.genscript.leads.service.ProductBundleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final ProductBundleService productBundleService;

    @PostMapping("/cleanup")
    public Map<String, Object> cleanup() {
        return adminService.cleanup();
    }

    @PostMapping("/product-bundles/initialize")
    public ProductBundleDtos.InitializeProductBundlesResponse initializeProductBundles() {
        return productBundleService.initializeFromMarkdown();
    }
}
