package com.example.resumebuilder.repository;

import com.example.resumebuilder.entity.ResumeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TemplateRepository extends JpaRepository<ResumeTemplate, Long> {
    Optional<ResumeTemplate> findByTemplateKey(String templateKey);
    List<ResumeTemplate> findByActiveTrueOrderByIdAsc();
    List<ResumeTemplate> findAllByOrderByIdAsc();
}
