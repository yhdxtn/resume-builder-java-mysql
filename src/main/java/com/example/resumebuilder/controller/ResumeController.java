package com.example.resumebuilder.controller;

import com.example.resumebuilder.dto.ResumeForm;
import com.example.resumebuilder.entity.AppUser;
import com.example.resumebuilder.entity.ResumeProfile;
import com.example.resumebuilder.repository.TemplateRepository;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

@Controller
@RequestMapping("/resume")
public class ResumeController {
    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;
    private final ResumeService resumeService;
    private final TemplateRenderService renderService;
    private final PdfService pdfService;

    public ResumeController(UserRepository userRepository, TemplateRepository templateRepository, ResumeService resumeService, TemplateRenderService renderService, PdfService pdfService) {
        this.userRepository = userRepository;
        this.templateRepository = templateRepository;
        this.resumeService = resumeService;
        this.renderService = renderService;
        this.pdfService = pdfService;
    }

    @GetMapping("/edit")
    public String edit(Model model, Principal principal) {
        AppUser user = currentUser(principal);
        ResumeProfile profile = resumeService.getOrCreate(user);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", resumeService.toForm(profile));
        }
        model.addAttribute("profile", profile);
        model.addAttribute("templates", templateRepository.findByActiveTrueOrderByIdAsc());
        return "resume/edit";
    }

    @PostMapping("/edit")
    public String save(@ModelAttribute("form") ResumeForm form,
                       @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                       Principal principal,
                       RedirectAttributes redirectAttributes) {
        try {
            resumeService.save(currentUser(principal), form, avatarFile);
            redirectAttributes.addFlashAttribute("success", "简历已保存，可以继续预览或下载 PDF。 ");
            return "redirect:/resume/preview";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/resume/edit";
        }
    }

    @GetMapping("/preview")
    public String preview(Model model, Principal principal) {
        ResumeProfile profile = resumeService.getOrCreate(currentUser(principal));
        model.addAttribute("resumeHtml", renderService.render(profile));
        model.addAttribute("profile", profile);
        return "resume/preview";
    }

    @GetMapping("/download")
    public void download(Principal principal, HttpServletResponse response) throws IOException {
        ResumeProfile profile = resumeService.getOrCreate(currentUser(principal));
        String html = renderService.render(profile);
        byte[] pdf = pdfService.toPdf(html);
        String name = profile.getFullName() == null || profile.getFullName().isBlank() ? "resume" : profile.getFullName();
        String fileName = URLEncoder.encode(name + "-简历.pdf", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName);
        response.getOutputStream().write(pdf);
    }

    private AppUser currentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName()).orElseThrow();
    }
}
