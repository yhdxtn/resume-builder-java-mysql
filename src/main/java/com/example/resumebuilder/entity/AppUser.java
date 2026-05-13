package com.example.resumebuilder.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 40)
    private String role = "USER";

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(length = 80)
    private String fullName;

    @Column(length = 80)
    private String jobTitle;

    @Column(length = 120)
    private String graduationSchool;

    @Column(length = 60)
    private String educationLevel;

    @Column(length = 40)
    private String phone;

    @Column(length = 120)
    private String contactEmail;

    @Column(length = 80)
    private String location;

    @Column(length = 160)
    private String website;

    @Column(length = 200)
    private String defaultAvatarPath;

    @Column(nullable = false)
    private Integer points = 100;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getGraduationSchool() { return graduationSchool; }
    public void setGraduationSchool(String graduationSchool) { this.graduationSchool = graduationSchool; }
    public String getEducationLevel() { return educationLevel; }
    public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getDefaultAvatarPath() { return defaultAvatarPath; }
    public void setDefaultAvatarPath(String defaultAvatarPath) { this.defaultAvatarPath = defaultAvatarPath; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
