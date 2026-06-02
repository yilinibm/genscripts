package com.genscript.leads.repository;

import com.genscript.leads.domain.ProductBundle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductBundleRepository extends JpaRepository<ProductBundle, UUID> {
    Optional<ProductBundle> findByCode(String code);
}
