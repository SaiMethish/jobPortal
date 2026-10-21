package com.example.jobPortal.repository;

import com.example.jobPortal.entity.AtsCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AtsCompanyRepository extends JpaRepository<AtsCompany, UUID> {

    List<AtsCompany> findByActiveTrue();

    List<AtsCompany> findByActiveTrueAndSource(String source);

    Optional<AtsCompany> findBySlugAndSource(String slug, String source);

    Optional<AtsCompany> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
