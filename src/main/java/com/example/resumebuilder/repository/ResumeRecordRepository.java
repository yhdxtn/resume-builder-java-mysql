package com.example.resumebuilder.repository;

import com.example.resumebuilder.entity.AppUser;
import com.example.resumebuilder.entity.ResumeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRecordRepository extends JpaRepository<ResumeRecord, Long> {
    List<ResumeRecord> findByUserOrderByCreatedAtDesc(AppUser user);
    Optional<ResumeRecord> findByIdAndUser(Long id, AppUser user);
    long countByUser(AppUser user);
}
