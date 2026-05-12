package com.example.resumebuilder.service;

import com.example.resumebuilder.entity.ResumeProfile;
import com.example.resumebuilder.entity.ResumeTemplate;
import com.example.resumebuilder.repository.TemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TemplateRenderService {
    private final TemplateRepository templateRepository;
    private final AvatarStorageService avatarStorageService;

    public TemplateRenderService(TemplateRepository templateRepository, AvatarStorageService avatarStorageService) {
        this.templateRepository = templateRepository;
        this.avatarStorageService = avatarStorageService;
    }

    public String render(ResumeProfile profile) {
        ResumeTemplate template = profile.getTemplate();
        if (template == null) {
            template = templateRepository.findByActiveTrueOrderByIdAsc().stream().findFirst().orElseThrow();
        }
        String html = template.getHtmlContent();
        Map<String, String> values = new LinkedHashMap<>();
        values.put("accentColor", safe(template.getAccentColor(), "#2563eb"));
        values.put("templateName", safe(template.getName(), "简历模板"));
        values.put("fullName", display(profile.getFullName(), "姓名"));
        values.put("jobTitle", display(profile.getJobTitle(), "求职岗位"));

        values.put("phone", visibleText(profile.getShowPhone(), profile.getPhone()));
        values.put("email", visibleText(profile.getShowEmail(), profile.getEmail()));
        values.put("location", visibleText(profile.getShowLocation(), profile.getLocation()));
        values.put("website", visibleText(profile.getShowWebsite(), profile.getWebsite()));
        values.put("contactLine", contactLine(profile, "　|　"));
        values.put("contactDot", contactLine(profile, " · "));
        values.put("contactStack", contactLine(profile, "<br/>"));
        values.put("avatarBlock", avatarBlock(profile));

        values.put("summary", visibleText(profile.getShowSummary(), profile.getSummary()));
        values.put("education", visibleText(profile.getShowEducation(), profile.getEducation()));
        values.put("experience", visibleText(profile.getShowExperience(), profile.getExperience()));
        values.put("projects", visibleText(profile.getShowProjects(), profile.getProjects()));
        values.put("skills", visibleText(profile.getShowSkills(), profile.getSkills()));
        values.put("certificates", visibleText(profile.getShowCertificates(), profile.getCertificates()));
        values.put("awards", visibleText(profile.getShowAwards(), profile.getAwards()));

        values.put("summarySection", section(profile.getShowSummary(), "个人优势", profile.getSummary()));
        values.put("educationSection", section(profile.getShowEducation(), "教育背景", profile.getEducation()));
        values.put("experienceSection", section(profile.getShowExperience(), "工作 / 实习经历", profile.getExperience()));
        values.put("projectsSection", section(profile.getShowProjects(), "项目经历", profile.getProjects()));
        values.put("skillsSection", section(profile.getShowSkills(), "技能", profile.getSkills()));
        values.put("certificatesSection", section(profile.getShowCertificates(), "证书", profile.getCertificates()));
        values.put("awardsSection", section(profile.getShowAwards(), "荣誉", profile.getAwards()));

        for (Map.Entry<String, String> entry : values.entrySet()) {
            html = html.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return html;
    }

    private String avatarBlock(ResumeProfile profile) {
        if (!defaultTrue(profile.getShowAvatar()) || isBlank(profile.getAvatarPath())) {
            return "";
        }
        String dataUri = avatarStorageService.toDataUri(profile.getAvatarPath());
        if (dataUri.isBlank()) {
            return "";
        }
        return "<div class=\"avatar-wrap\"><img class=\"avatar\" src=\"" + dataUri + "\" alt=\"avatar\"/></div>";
    }

    private String section(Boolean visible, String title, String text) {
        if (!defaultTrue(visible) || isBlank(text)) {
            return "";
        }
        return "<div class=\"resume-section\"><div class=\"section-title\">"
                + HtmlUtils.htmlEscape(title)
                + "</div><div class=\"text\">"
                + display(text, "")
                + "</div></div>";
    }

    private String contactLine(ResumeProfile profile, String separator) {
        List<String> items = new ArrayList<>();
        addVisible(items, profile.getShowPhone(), profile.getPhone());
        addVisible(items, profile.getShowEmail(), profile.getEmail());
        addVisible(items, profile.getShowLocation(), profile.getLocation());
        addVisible(items, profile.getShowWebsite(), profile.getWebsite());
        return String.join(separator, items);
    }

    private void addVisible(List<String> items, Boolean visible, String text) {
        if (defaultTrue(visible) && !isBlank(text)) {
            items.add(display(text, ""));
        }
    }

    private String visibleText(Boolean visible, String text) {
        if (!defaultTrue(visible) || isBlank(text)) {
            return "";
        }
        return display(text, "");
    }

    private String display(String text, String fallback) {
        String v = (text == null || text.isBlank()) ? fallback : text;
        return HtmlUtils.htmlEscape(v).replace("\r\n", "\n").replace("\r", "\n").replace("\n", "<br/>");
    }

    private String safe(String text, String fallback) {
        return (text == null || text.isBlank()) ? fallback : text.trim();
    }

    private boolean defaultTrue(Boolean value) {
        return value == null || value;
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
