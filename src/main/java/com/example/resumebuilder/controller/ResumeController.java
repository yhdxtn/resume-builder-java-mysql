package com.example.resumebuilder.controller;

import com.example.resumebuilder.dto.ResumeForm;
import com.example.resumebuilder.entity.AppUser;
import com.example.resumebuilder.entity.ResumeProfile;
import com.example.resumebuilder.entity.ResumeTemplate;
import com.example.resumebuilder.entity.ResumeRecord;
import com.example.resumebuilder.repository.TemplateRepository;
import com.example.resumebuilder.repository.ResumeRecordRepository;
import com.example.resumebuilder.repository.UserRepository;
import com.example.resumebuilder.service.PdfService;
import com.example.resumebuilder.service.ResumeService;
import com.example.resumebuilder.service.TemplateRenderService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/resume")
public class ResumeController {
    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;
    private final ResumeService resumeService;
    private final TemplateRenderService renderService;
    private final PdfService pdfService;
    private final ResumeRecordRepository recordRepository;

    public ResumeController(UserRepository userRepository, TemplateRepository templateRepository, ResumeService resumeService, TemplateRenderService renderService, PdfService pdfService, ResumeRecordRepository recordRepository) {
        this.userRepository = userRepository;
        this.templateRepository = templateRepository;
        this.resumeService = resumeService;
        this.renderService = renderService;
        this.pdfService = pdfService;
        this.recordRepository = recordRepository;
    }

    @GetMapping("/edit")
    public String edit(@RequestParam(value = "templateId", required = false) Long templateId,
                       Model model,
                       Principal principal) {
        AppUser user = currentUser(principal);
        ResumeProfile profile = resumeService.getOrCreate(user);
        ResumeForm form;
        if (model.containsAttribute("form")) {
            form = (ResumeForm) model.asMap().get("form");
        } else {
            form = resumeService.toForm(profile);
        }
        List<ResumeTemplate> templates = templateRepository.findByActiveTrueOrderByIdAsc();
        ResumeTemplate selectedTemplate = null;

        if (templateId != null) {
            selectedTemplate = templateRepository.findById(templateId).filter(ResumeTemplate::isActive).orElse(null);
            if (selectedTemplate != null) {
                form.setTemplateId(selectedTemplate.getId());
            }
        }

        if (selectedTemplate == null && form.getTemplateId() != null) {
            selectedTemplate = templateRepository.findById(form.getTemplateId()).filter(ResumeTemplate::isActive).orElse(null);
        }

        if (selectedTemplate == null && profile.getTemplate() != null && profile.getTemplate().isActive()) {
            selectedTemplate = profile.getTemplate();
            form.setTemplateId(selectedTemplate.getId());
        }

        if (selectedTemplate == null && !templates.isEmpty()) {
            selectedTemplate = templates.get(0);
            form.setTemplateId(selectedTemplate.getId());
        }

        ResumeProfile previewProfile = resumeService.previewProfile(user, profile, form);
        model.addAttribute("form", form);
        model.addAttribute("profile", profile);
        model.addAttribute("selectedTemplate", selectedTemplate);
        model.addAttribute("resumePreviewHtml", renderService.renderEditorPreview(previewProfile));
        return "resume/edit";
    }

    @PostMapping("/edit")
    public String save(@ModelAttribute("form") ResumeForm form,
                       @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                       @RequestParam(value = "action", required = false, defaultValue = "publish") String action,
                       Principal principal,
                       RedirectAttributes redirectAttributes) {
        try {
            AppUser user = currentUser(principal);
            ResumeProfile profile = resumeService.save(user, form, avatarFile);
            if ("draft".equalsIgnoreCase(action)) {
                redirectAttributes.addFlashAttribute("success", "草稿已保存，下次可从“草稿箱”继续编辑。 ");
                return "redirect:/dashboard#drafts";
            }
            String html = renderService.render(profile);
            ResumeRecord record = new ResumeRecord();
            record.setUser(user);
            record.setTemplate(profile.getTemplate());
            record.setFullName(profile.getFullName());
            record.setJobTitle(profile.getJobTitle());
            record.setTemplateName(profile.getTemplate() == null ? "未选择模板" : profile.getTemplate().getName());
            String baseName = (profile.getFullName() == null || profile.getFullName().isBlank()) ? "我的简历" : profile.getFullName() + "的简历";
            record.setTitle(baseName + " · " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
            record.setRenderedHtml(html);
            ResumeRecord savedRecord = recordRepository.save(record);
            redirectAttributes.addFlashAttribute("success", "简历已保存到“我的简历”，可以预览或下载 PDF。 ");
            return "redirect:/resume/preview?recordId=" + savedRecord.getId();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/resume/edit";
        }
    }


    @PostMapping(value = "/live-preview", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String livePreview(@ModelAttribute ResumeForm form, Principal principal) {
        AppUser user = currentUser(principal);
        ResumeProfile savedProfile = resumeService.getOrCreate(user);
        ResumeProfile previewProfile = resumeService.previewProfile(user, savedProfile, form);
        return renderService.renderEditorPreview(previewProfile);
    }

    @GetMapping("/preview")
    public String preview(@RequestParam(value = "recordId", required = false) Long recordId,
                          Model model, Principal principal) {
        AppUser user = currentUser(principal);
        if (recordId != null) {
            ResumeRecord record = recordRepository.findByIdAndUser(recordId, user).orElse(null);
            if (record != null && record.getRenderedHtml() != null && !record.getRenderedHtml().isBlank()) {
                model.addAttribute("resumeHtml", record.getRenderedHtml());
                model.addAttribute("record", record);
                model.addAttribute("profile", resumeService.getOrCreate(user));
                return "resume/preview";
            }
        }
        ResumeProfile profile = resumeService.getOrCreate(user);
        model.addAttribute("resumeHtml", renderService.render(profile));
        model.addAttribute("profile", profile);
        return "resume/preview";
    }

    @GetMapping("/download")
    public void download(@RequestParam(value = "recordId", required = false) Long recordId,
                         Principal principal, HttpServletResponse response) throws IOException {
        AppUser user = currentUser(principal);
        ResumeProfile currentProfile = resumeService.getOrCreate(user);
        String html;
        String name;
        if (recordId != null) {
            ResumeRecord record = recordRepository.findByIdAndUser(recordId, user).orElse(null);
            if (record != null && record.getRenderedHtml() != null && !record.getRenderedHtml().isBlank()) {
                html = record.getRenderedHtml();
                name = record.getTitle() == null || record.getTitle().isBlank() ? "resume" : record.getTitle();
            } else {
                html = renderService.render(currentProfile);
                name = currentProfile.getFullName() == null || currentProfile.getFullName().isBlank() ? "resume" : currentProfile.getFullName();
            }
        } else {
            html = renderService.render(currentProfile);
            name = currentProfile.getFullName() == null || currentProfile.getFullName().isBlank() ? "resume" : currentProfile.getFullName();
        }
        writePdf(response, html, name);
    }

    @PostMapping("/download-current")
    public void downloadCurrent(@ModelAttribute("form") ResumeForm form,
                                @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                Principal principal,
                                HttpServletResponse response) throws IOException {
        AppUser user = currentUser(principal);
        ResumeProfile profile = resumeService.save(user, form, avatarFile);
        String html = renderService.render(profile);
        String name = profile.getFullName() == null || profile.getFullName().isBlank() ? "resume" : profile.getFullName();
        writePdf(response, html, name);
    }

    private void writePdf(HttpServletResponse response, String html, String name) throws IOException {
        byte[] pdf = pdfService.toPdf(html);
        String safeName = name == null || name.isBlank() ? "resume" : name;
        String fileName = URLEncoder.encode(safeName + "-简历.pdf", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName);
        response.getOutputStream().write(pdf);
    }

    @GetMapping("/my")
    public String myResumes(Model model, Principal principal) {
        AppUser user = currentUser(principal);
        ResumeProfile draft = resumeService.getOrCreate(user);
        model.addAttribute("draft", draft);
        model.addAttribute("records", recordRepository.findByUserOrderByCreatedAtDesc(user));
        return "resume/list";
    }

    private AppUser currentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName()).orElseThrow();
    }
}
