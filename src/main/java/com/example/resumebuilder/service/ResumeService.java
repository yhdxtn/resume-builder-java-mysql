package com.example.resumebuilder.service;

import com.example.resumebuilder.dto.ResumeForm;
import com.example.resumebuilder.entity.AppUser;
import com.example.resumebuilder.entity.ResumeProfile;
import com.example.resumebuilder.entity.ResumeTemplate;
import com.example.resumebuilder.repository.ResumeProfileRepository;
import com.example.resumebuilder.repository.TemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeService {
    private final ResumeProfileRepository profileRepository;
    private final TemplateRepository templateRepository;
    private final AvatarStorageService avatarStorageService;

    public ResumeService(ResumeProfileRepository profileRepository, TemplateRepository templateRepository, AvatarStorageService avatarStorageService) {
        this.profileRepository = profileRepository;
        this.templateRepository = templateRepository;
        this.avatarStorageService = avatarStorageService;
    }

    @Transactional
    public ResumeProfile getOrCreate(AppUser user) {
        return profileRepository.findByUser(user).orElseGet(() -> {
            ResumeProfile p = new ResumeProfile();
            p.setUser(user);
            p.setFullName(user.getFullName());
            p.setEmail(user.getUsername());
            templateRepository.findByActiveTrueOrderByIdAsc().stream().findFirst().ifPresent(p::setTemplate);
            return profileRepository.save(p);
        });
    }

    @Transactional
    public ResumeProfile save(AppUser user, ResumeForm form, MultipartFile avatarFile) {
        ResumeProfile profile = getOrCreate(user);
        if (form.getTemplateId() != null) {
            ResumeTemplate template = templateRepository.findById(form.getTemplateId()).orElse(null);
            profile.setTemplate(template);
        }
        profile.setFullName(form.getFullName());
        profile.setJobTitle(form.getJobTitle());
        profile.setPhone(form.getPhone());
        profile.setEmail(form.getEmail());
        profile.setLocation(form.getLocation());
        profile.setWebsite(form.getWebsite());

        if (Boolean.TRUE.equals(form.getRemoveAvatar())) {
            profile.setAvatarPath(null);
        }
        String newAvatarPath = avatarStorageService.storeAvatar(user, avatarFile);
        if (newAvatarPath != null) {
            profile.setAvatarPath(newAvatarPath);
        }

        profile.setShowAvatar(bool(form.getShowAvatar()));
        profile.setShowPhone(bool(form.getShowPhone()));
        profile.setShowEmail(bool(form.getShowEmail()));
        profile.setShowLocation(bool(form.getShowLocation()));
        profile.setShowWebsite(bool(form.getShowWebsite()));

        profile.setSummary(form.getSummary());
        profile.setEducation(form.getEducation());
        profile.setExperience(form.getExperience());
        profile.setProjects(form.getProjects());
        profile.setSkills(form.getSkills());
        profile.setCertificates(form.getCertificates());
        profile.setAwards(form.getAwards());

        profile.setShowSummary(bool(form.getShowSummary()));
        profile.setShowEducation(bool(form.getShowEducation()));
        profile.setShowExperience(bool(form.getShowExperience()));
        profile.setShowProjects(bool(form.getShowProjects()));
        profile.setShowSkills(bool(form.getShowSkills()));
        profile.setShowCertificates(bool(form.getShowCertificates()));
        profile.setShowAwards(bool(form.getShowAwards()));
        return profileRepository.save(profile);
    }

    public ResumeForm toForm(ResumeProfile p) {
        ResumeForm f = new ResumeForm();
        f.setTemplateId(p.getTemplate() == null ? null : p.getTemplate().getId());
        f.setFullName(p.getFullName());
        f.setJobTitle(p.getJobTitle());
        f.setPhone(p.getPhone());
        f.setEmail(p.getEmail());
        f.setLocation(p.getLocation());
        f.setWebsite(p.getWebsite());
        f.setShowAvatar(defaultTrue(p.getShowAvatar()));
        f.setRemoveAvatar(false);
        f.setShowPhone(defaultTrue(p.getShowPhone()));
        f.setShowEmail(defaultTrue(p.getShowEmail()));
        f.setShowLocation(defaultTrue(p.getShowLocation()));
        f.setShowWebsite(defaultTrue(p.getShowWebsite()));
        f.setSummary(p.getSummary());
        f.setEducation(p.getEducation());
        f.setExperience(p.getExperience());
        f.setProjects(p.getProjects());
        f.setSkills(p.getSkills());
        f.setCertificates(p.getCertificates());
        f.setAwards(p.getAwards());
        f.setShowSummary(defaultTrue(p.getShowSummary()));
        f.setShowEducation(defaultTrue(p.getShowEducation()));
        f.setShowExperience(defaultTrue(p.getShowExperience()));
        f.setShowProjects(defaultTrue(p.getShowProjects()));
        f.setShowSkills(defaultTrue(p.getShowSkills()));
        f.setShowCertificates(defaultTrue(p.getShowCertificates()));
        f.setShowAwards(defaultTrue(p.getShowAwards()));
        return f;
    }

    private boolean bool(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    private boolean defaultTrue(Boolean value) {
        return value == null || value;
    }
}
