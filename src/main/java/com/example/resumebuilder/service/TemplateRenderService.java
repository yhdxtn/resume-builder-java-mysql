package com.example.resumebuilder.service;

import com.example.resumebuilder.entity.ResumeProfile;
import com.example.resumebuilder.entity.ResumeTemplate;
import com.example.resumebuilder.repository.TemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        return renderInternal(profile, template, false, false);
    }

    public String renderWithTemplate(ResumeProfile profile, ResumeTemplate template) {
        return renderInternal(profile, template, false, false);
    }

    public String renderMiniPreview(ResumeTemplate template) {
        return renderInternal(sampleProfile(), template, true, true);
    }

    public String renderEditorPreview(ResumeProfile profile) {
        ResumeTemplate template = profile.getTemplate();
        if (template == null) {
            template = templateRepository.findByActiveTrueOrderByIdAsc().stream().findFirst().orElseThrow();
        }
        return toEditorPreviewHtml(renderInternal(profile, template, false, false));
    }

    public Map<Long, String> renderMiniPreviewMap(List<ResumeTemplate> templates) {
        return templates.stream().collect(Collectors.toMap(
                ResumeTemplate::getId,
                this::renderMiniPreview,
                (a, b) -> a,
                LinkedHashMap::new
        ));
    }

    private String renderInternal(ResumeProfile profile, ResumeTemplate template, boolean usePlaceholderAvatar, boolean miniMode) {
        String html = template.getHtmlContent();
        Map<String, String> values = new LinkedHashMap<>();
        values.put("accentColor", safe(template.getAccentColor(), "#2563eb"));
        values.put("templateName", safe(template.getName(), "简历模板"));
        values.put("fullName", display(profile.getFullName(), "姓名"));
        values.put("jobTitle", display(profile.getJobTitle(), "求职岗位"));
        values.put("graduationSchool", visibleText(profile.getShowGraduationSchool(), profile.getGraduationSchool()));
        values.put("educationLevel", visibleText(profile.getShowEducationLevel(), profile.getEducationLevel()));

        values.put("phone", visibleText(profile.getShowPhone(), profile.getPhone()));
        values.put("email", visibleText(profile.getShowEmail(), profile.getEmail()));
        values.put("location", visibleText(profile.getShowLocation(), profile.getLocation()));
        values.put("website", visibleText(profile.getShowWebsite(), profile.getWebsite()));
        values.put("contactLine", contactLine(profile, "　|　"));
        values.put("contactDot", contactLine(profile, " · "));
        values.put("contactStack", contactLine(profile, "<br/>"));
        values.put("avatarBlock", avatarBlock(profile, usePlaceholderAvatar));

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
        return miniMode ? toMiniPreviewHtml(html) : html;
    }

    private ResumeProfile sampleProfile() {
        ResumeProfile p = new ResumeProfile();
        p.setFullName("林知远");
        p.setJobTitle("求职意向：Java开发工程师");
        p.setPhone("16888888888");
        p.setEmail("jianli@qq.com");
        p.setGraduationSchool("文山学院");
        p.setEducationLevel("本科");
        p.setLocation("上海");
        p.setWebsite("github.com/careercraft");
        p.setSummary("熟悉 Java、Spring Boot 与 MySQL，能独立完成后台管理系统开发。\n具备小程序开发、Linux 运维和项目文档整理经验，沟通执行力较强。");
        p.setEducation("2018.09 - 2022.06　文山学院　计算机科学与技术（本科）\n主修课程：Java 程序设计、数据库原理、Web 开发、软件工程。");
        p.setExperience("2024.01 - 至今　云南宏桥新能源有限公司　综合管理 / 运维助理\n- 负责办公台账、物资管理、数据统计与部门协调工作。\n- 参与小程序页面维护、电脑网络故障排查和基础服务器运维。\n- 协助整理项目资料，提高内部信息流转效率。");
        p.setProjects("简历生成网站　Java + Spring Boot + MySQL\n- 实现登录注册、模板管理、头像上传、在线编辑和 PDF 导出。\n- 设计多套简历模板，支持后台定期更新与启用停用。");
        p.setSkills("Java / Spring Boot / MySQL / HTML / CSS / JavaScript\nLinux 基础运维、微信小程序、Office 文档与数据表处理。");
        p.setCertificates("大学英语四级、计算机二级、普通话证书");
        p.setAwards("优秀学生干部、校级奖学金、创新创业项目优秀成员");
        p.setShowAvatar(true);
        p.setShowPhone(true);
        p.setShowEmail(true);
        p.setShowLocation(true);
        p.setShowWebsite(true);
        p.setShowGraduationSchool(true);
        p.setShowEducationLevel(true);
        p.setShowSummary(true);
        p.setShowEducation(true);
        p.setShowExperience(true);
        p.setShowProjects(true);
        p.setShowSkills(true);
        p.setShowCertificates(true);
        p.setShowAwards(true);
        return p;
    }


    private String toEditorPreviewHtml(String html) {
        html = removeCssBlock(html, "@media screen and (max-width: 820px)");
        String previewCss = """
        html, body { margin: 0 !important; width: 100% !important; min-height: 100% !important; overflow-x: hidden !important; background: transparent !important; }
        body { display: flex !important; justify-content: center !important; align-items: flex-start !important; padding: 10px 0 28px !important; }
        .page { width: 210mm !important; min-height: 297mm !important; margin: 0 !important; transform: scale(.62) !important; transform-origin: top center !important; box-shadow: 0 18px 45px rgba(15,23,42,.18) !important; border-radius: 8px !important; flex: 0 0 auto !important; overflow: hidden !important; }
        """;
        if (html.contains("</style>")) {
            return html.replace("</style>", previewCss + "\n</style>");
        }
        return html.replace("</head>", "<style>" + previewCss + "</style></head>");
    }

    private String toMiniPreviewHtml(String html) {
        html = removeCssBlock(html, "@media screen and (max-width: 820px)");
        String previewCss = """
        html, body { margin: 0 !important; width: 100% !important; height: 100% !important; overflow: hidden !important; background: #eef2f7 !important; }
        body { display: flex !important; justify-content: center !important; align-items: flex-start !important; padding: 10px 0 !important; }
        .page { width: 210mm !important; min-height: 297mm !important; transform: scale(.34) !important; transform-origin: top center !important; margin: 0 !important; box-shadow: 0 18px 45px rgba(15,23,42,.18) !important; border-radius: 0 !important; flex: 0 0 auto !important; }
        """;
        if (html.contains("</style>")) {
            return html.replace("</style>", previewCss + "\n</style>");
        }
        return html.replace("</head>", "<style>" + previewCss + "</style></head>");
    }

    private String removeCssBlock(String html, String marker) {
        int markerIndex = html.indexOf(marker);
        if (markerIndex < 0) {
            return html;
        }
        int openBrace = html.indexOf('{', markerIndex);
        if (openBrace < 0) {
            return html;
        }
        int depth = 0;
        for (int i = openBrace; i < html.length(); i++) {
            char c = html.charAt(i);
            if (c == '{') depth++;
            if (c == '}') depth--;
            if (depth == 0) {
                return html.substring(0, markerIndex) + html.substring(i + 1);
            }
        }
        return html;
    }

    private String avatarBlock(ResumeProfile profile, boolean usePlaceholderAvatar) {
        if (!defaultTrue(profile.getShowAvatar())) {
            return "";
        }
        String dataUri = "";
        String avatarPath = profile.getAvatarPath();
        if (isBlank(avatarPath) && profile.getUser() != null) {
            avatarPath = profile.getUser().getDefaultAvatarPath();
        }
        if (!isBlank(avatarPath)) {
            dataUri = avatarStorageService.toDataUri(avatarPath);
        }
        if (dataUri.isBlank() && usePlaceholderAvatar) {
            dataUri = placeholderAvatarDataUri();
        }
        if (dataUri.isBlank()) {
            return "";
        }
        return "<div class=\"avatar-wrap\"><img class=\"avatar\" src=\"" + dataUri + "\" alt=\"avatar\"/></div>";
    }

    private String placeholderAvatarDataUri() {
        String svg = """
        <svg xmlns='http://www.w3.org/2000/svg' width='240' height='240' viewBox='0 0 240 240'>
          <rect width='240' height='240' rx='16' fill='#f4f7fb'/>
          <circle cx='120' cy='88' r='42' fill='#f1c7a8'/>
          <path d='M72 202c4-41 30-66 48-66s44 25 48 66z' fill='#1f2937'/>
          <path d='M80 84c2-34 25-54 47-50 25 4 40 24 35 57-13-20-31-16-50-27-8 10-19 13-32 20z' fill='#6b3f2b'/>
          <circle cx='104' cy='92' r='4' fill='#111827'/>
          <circle cx='136' cy='92' r='4' fill='#111827'/>
          <path d='M105 113c10 8 21 8 31 0' fill='none' stroke='#b45309' stroke-width='4' stroke-linecap='round'/>
          <path d='M96 150h48l-24 28z' fill='#fff'/>
        </svg>
        """;
        return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
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
        addVisible(items, profile.getShowGraduationSchool(), profile.getGraduationSchool());
        addVisible(items, profile.getShowEducationLevel(), profile.getEducationLevel());
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
