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
        return profileRepository.findByUser(user)
                .map(profile -> fillBlankFieldsFromUser(profile, user))
                .orElseGet(() -> {
                    ResumeProfile p = new ResumeProfile();
                    p.setUser(user);
                    p.setFullName(user.getFullName());
                    p.setJobTitle(user.getJobTitle());
                    p.setGraduationSchool(user.getGraduationSchool());
                    p.setEducationLevel(user.getEducationLevel());
                    p.setPhone(user.getPhone());
                    p.setEmail(emailCandidate(user));
                    p.setLocation(user.getLocation());
                    p.setWebsite(user.getWebsite());
                    p.setAvatarPath(user.getDefaultAvatarPath());
                    templateRepository.findByActiveTrueOrderByIdAsc().stream().findFirst().ifPresent(p::setTemplate);
                    return profileRepository.save(p);
                });
    }

    private ResumeProfile fillBlankFieldsFromUser(ResumeProfile profile, AppUser user) {
        profile.setUser(user);
        boolean changed = false;
        if (isBlank(profile.getFullName()) && !isBlank(user.getFullName())) { profile.setFullName(user.getFullName()); changed = true; }
        if (isBlank(profile.getJobTitle()) && !isBlank(user.getJobTitle())) { profile.setJobTitle(user.getJobTitle()); changed = true; }
        if (isBlank(profile.getGraduationSchool()) && !isBlank(user.getGraduationSchool())) { profile.setGraduationSchool(user.getGraduationSchool()); changed = true; }
        if (isBlank(profile.getEducationLevel()) && !isBlank(user.getEducationLevel())) { profile.setEducationLevel(user.getEducationLevel()); changed = true; }
        if (isBlank(profile.getPhone()) && !isBlank(user.getPhone())) { profile.setPhone(user.getPhone()); changed = true; }
        if (isBlank(profile.getEmail()) || (!looksLikeEmail(profile.getEmail()) && looksLikeEmail(user.getContactEmail()))) { profile.setEmail(emailCandidate(user)); changed = true; }
        if (isBlank(profile.getLocation()) && !isBlank(user.getLocation())) { profile.setLocation(user.getLocation()); changed = true; }
        if (isBlank(profile.getWebsite()) && !isBlank(user.getWebsite())) { profile.setWebsite(user.getWebsite()); changed = true; }
        if (isBlank(profile.getAvatarPath()) && !isBlank(user.getDefaultAvatarPath())) { profile.setAvatarPath(user.getDefaultAvatarPath()); changed = true; }
        return changed ? profileRepository.save(profile) : profile;
    }

    @Transactional
    public ResumeProfile save(AppUser user, ResumeForm form, MultipartFile avatarFile) {
        ResumeProfile profile = getOrCreate(user);
        applyForm(profile, form);

        if (Boolean.TRUE.equals(form.getRemoveAvatar())) {
            profile.setAvatarPath(null);
        }
        String croppedAvatarPath = avatarStorageService.storeDataUriAvatar(user, form.getPreviewAvatarDataUri());
        if (croppedAvatarPath != null) {
            profile.setAvatarPath(croppedAvatarPath);
        } else {
            String newAvatarPath = avatarStorageService.storeAvatar(user, avatarFile);
            if (newAvatarPath != null) {
                profile.setAvatarPath(newAvatarPath);
            }
        }
        return profileRepository.save(profile);
    }

    /**
     * 构造一个不入库的临时简历对象，用于编辑页右侧/左侧实时预览。
     * 头像文件本身仍在用户点击保存后才上传；实时预览会使用当前已保存头像。
     */
    public ResumeProfile previewProfile(AppUser user, ResumeProfile savedProfile, ResumeForm form) {
        ResumeProfile p = new ResumeProfile();
        p.setUser(user);
        String previewAvatar = normalizePreviewAvatar(form.getPreviewAvatarDataUri());
        if (Boolean.TRUE.equals(form.getRemoveAvatar())) {
            p.setAvatarPath(null);
        } else if (previewAvatar != null) {
            // 用户刚选择头像但还没保存时，实时预览先使用浏览器传来的临时 data URI。
            p.setAvatarPath(previewAvatar);
        } else {
            p.setAvatarPath(firstNonBlank(savedProfile.getAvatarPath(), user.getDefaultAvatarPath()));
        }
        applyForm(p, form);
        if (p.getTemplate() == null) {
            p.setTemplate(savedProfile.getTemplate());
        }
        return p;
    }


    private String normalizePreviewAvatar(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        // 控制长度，避免用户传入超大字符串影响实时预览接口。
        if (trimmed.length() > 2_000_000) {
            return null;
        }
        String lower = trimmed.toLowerCase();
        if (lower.startsWith("data:image/") && lower.contains(";base64,")) {
            return trimmed;
        }
        return null;
    }

    private void applyForm(ResumeProfile profile, ResumeForm form) {
        if (form.getTemplateId() != null) {
            ResumeTemplate template = templateRepository.findById(form.getTemplateId()).filter(ResumeTemplate::isActive).orElse(null);
            profile.setTemplate(template);
        }
        profile.setFullName(form.getFullName());
        profile.setJobTitle(form.getJobTitle());
        profile.setGraduationSchool(form.getGraduationSchool());
        profile.setEducationLevel(form.getEducationLevel());
        profile.setPhone(form.getPhone());
        profile.setEmail(form.getEmail());
        profile.setLocation(form.getLocation());
        profile.setWebsite(form.getWebsite());

        profile.setShowAvatar(bool(form.getShowAvatar()));
        profile.setShowPhone(bool(form.getShowPhone()));
        profile.setShowEmail(bool(form.getShowEmail()));
        profile.setShowLocation(bool(form.getShowLocation()));
        profile.setShowWebsite(bool(form.getShowWebsite()));
        profile.setShowGraduationSchool(bool(form.getShowGraduationSchool()));
        profile.setShowEducationLevel(bool(form.getShowEducationLevel()));

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
    }

    public ResumeForm toForm(ResumeProfile p) {
        ResumeForm f = new ResumeForm();
        f.setTemplateId(p.getTemplate() == null ? null : p.getTemplate().getId());
        AppUser user = p.getUser();
        f.setFullName(firstNonBlank(p.getFullName(), user == null ? null : user.getFullName()));
        f.setJobTitle(firstNonBlank(p.getJobTitle(), user == null ? null : user.getJobTitle()));
        f.setGraduationSchool(firstNonBlank(p.getGraduationSchool(), user == null ? null : user.getGraduationSchool()));
        f.setEducationLevel(firstNonBlank(p.getEducationLevel(), user == null ? null : user.getEducationLevel()));
        f.setPhone(firstNonBlank(p.getPhone(), user == null ? null : user.getPhone()));
        f.setEmail(preferredEmail(p.getEmail(), user));
        f.setLocation(firstNonBlank(p.getLocation(), user == null ? null : user.getLocation()));
        f.setWebsite(firstNonBlank(p.getWebsite(), user == null ? null : user.getWebsite()));
        f.setShowAvatar(defaultTrue(p.getShowAvatar()));
        f.setRemoveAvatar(false);
        f.setShowPhone(defaultTrue(p.getShowPhone()));
        f.setShowEmail(defaultTrue(p.getShowEmail()));
        f.setShowLocation(defaultTrue(p.getShowLocation()));
        f.setShowWebsite(defaultTrue(p.getShowWebsite()));
        f.setShowGraduationSchool(defaultTrue(p.getShowGraduationSchool()));
        f.setShowEducationLevel(defaultTrue(p.getShowEducationLevel()));
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    private String preferredEmail(String profileEmail, AppUser user) {
        if (looksLikeEmail(profileEmail)) {
            return profileEmail;
        }
        return emailCandidate(user);
    }

    private String emailCandidate(AppUser user) {
        if (user == null) {
            return null;
        }
        if (looksLikeEmail(user.getContactEmail())) {
            return user.getContactEmail();
        }
        if (looksLikeEmail(user.getUsername())) {
            return user.getUsername();
        }
        return null;
    }

    private boolean looksLikeEmail(String value) {
        return value != null && value.contains("@") && value.indexOf('@') > 0 && value.indexOf('@') < value.length() - 1;
    }

    private boolean bool(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    private boolean defaultTrue(Boolean value) {
        return value == null || value;
    }
}
