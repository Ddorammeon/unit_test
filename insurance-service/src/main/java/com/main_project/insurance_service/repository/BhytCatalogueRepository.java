package com.main_project.insurance_service.repository;

import com.main_project.insurance_service.entity.BhytCatalogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BhytCatalogueRepository extends JpaRepository<BhytCatalogue, UUID> {
    Optional<BhytCatalogue> findByServiceCode(String serviceCode);
}

