package com.example.resumebuilder.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resume_profiles")
public class ResumeProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "template_id")
    private ResumeTemplate template;

    @Column(length = 80)
    private String fullName;
    @Column(length = 120)
    private String jobTitle;
    @Column(length = 40)
    private String phone;
    @Column(length = 120)
    private String email;
    @Column(length = 120)
    private String location;
    @Column(length = 160)
    private String website;
    @Column(length = 260)
    private String avatarPath;

    @Column
    private Boolean showAvatar = true;
    @Column
    private Boolean showPhone = true;
    @Column
    private Boolean showEmail = true;
    @Column
    private Boolean showLocation = true;
    @Column
    private Boolean showWebsite = true;

    @Lob @Column(columnDefinition = "TEXT")
    private String summary;
    @Lob @Column(columnDefinition = "TEXT")
    private String education;
    @Lob @Column(columnDefinition = "TEXT")
    private String experience;
    @Lob @Column(columnDefinition = "TEXT")
    private String projects;
    @Lob @Column(columnDefinition = "TEXT")
    private String skills;
    @Lob @Column(columnDefinition = "TEXT")
    private String certificates;
    @Lob @Column(columnDefinition = "TEXT")
    private String awards;

    @Column
    private Boolean showSummary = true;
    @Column
    private Boolean showEducation = true;
    @Column
    private Boolean showExperience = true;
    @Column
    private Boolean showProjects = true;
    @Column
    private Boolean showSkills = true;
    @Column
    private Boolean showCertificates = true;
    @Column
    private Boolean showAwards = true;

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        this.updatedAt = LocalDateTime.now();
        normalizeBooleans();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        normalizeBooleans();
    }

    private void normalizeBooleans() {
        if (showAvatar == null) showAvatar = true;
        if (showPhone == null) showPhone = true;
        if (showEmail == null) showEmail = true;
        if (showLocation == null) showLocation = true;
        if (showWebsite == null) showWebsite = true;
        if (showSummary == null) showSummary = true;
        if (showEducation == null) showEducation = true;
        if (showExperience == null) showExperience = true;
        if (showProjects == null) showProjects = true;
        if (showSkills == null) showSkills = true;
        if (showCertificates == null) showCertificates = true;
        if (showAwards == null) showAwards = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public ResumeTemplate getTemplate() { return template; }
    public void setTemplate(ResumeTemplate template) { this.template = template; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }
    public Boolean getShowAvatar() { return showAvatar; }
    public void setShowAvatar(Boolean showAvatar) { this.showAvatar = showAvatar; }
    public Boolean getShowPhone() { return showPhone; }
    public void setShowPhone(Boolean showPhone) { this.showPhone = showPhone; }
    public Boolean getShowEmail() { return showEmail; }
    public void setShowEmail(Boolean showEmail) { this.showEmail = showEmail; }
    public Boolean getShowLocation() { return showLocation; }
    public void setShowLocation(Boolean showLocation) { this.showLocation = showLocation; }
    public Boolean getShowWebsite() { return showWebsite; }
    public void setShowWebsite(Boolean showWebsite) { this.showWebsite = showWebsite; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public String getProjects() { return projects; }
    public void setProjects(String projects) { this.projects = projects; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public String getCertificates() { return certificates; }
    public void setCertificates(String certificates) { this.certificates = certificates; }
    public String getAwards() { return awards; }
    public void setAwards(String awards) { this.awards = awards; }
    public Boolean getShowSummary() { return showSummary; }
    public void setShowSummary(Boolean showSummary) { this.showSummary = showSummary; }
    public Boolean getShowEducation() { return showEducation; }
    public void setShowEducation(Boolean showEducation) { this.showEducation = showEducation; }
    public Boolean getShowExperience() { return showExperience; }
    public void setShowExperience(Boolean showExperience) { this.showExperience = showExperience; }
    public Boolean getShowProjects() { return showProjects; }
    public void setShowProjects(Boolean showProjects) { this.showProjects = showProjects; }
    public Boolean getShowSkills() { return showSkills; }
    public void setShowSkills(Boolean showSkills) { this.showSkills = showSkills; }
    public Boolean getShowCertificates() { return showCertificates; }
    public void setShowCertificates(Boolean showCertificates) { this.showCertificates = showCertificates; }
    public Boolean getShowAwards() { return showAwards; }
    public void setShowAwards(Boolean showAwards) { this.showAwards = showAwards; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
