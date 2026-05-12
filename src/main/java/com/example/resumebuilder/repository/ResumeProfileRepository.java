package com.example.resumebuilder.repository;

import com.example.resumebuilder.entity.AppUser;
import com.example.resumebuilder.entity.ResumeProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeProfileRepository extends JpaRepository<ResumeProfile, Long> {
    Optional<ResumeProfile> findByUser(AppUser user);
}
