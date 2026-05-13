package com.example.resumebuilder.controller;

import com.example.resumebuilder.entity.AppUser;
import com.example.resumebuilder.entity.ResumeProfile;
import com.example.resumebuilder.entity.ResumeTemplate;
import com.example.resumebuilder.repository.TemplateRepository;
import com.example.resumebuilder.repository.ResumeRecordRepository;
import com.example.resumebuilder.repository.UserRepository;
import com.example.resumebuilder.service.ResumeService;
import com.example.resumebuilder.service.TemplateRenderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class HomeController {
    private final TemplateRepository templateRepository;
    private final UserRepository userRepository;
    private final ResumeService resumeService;
    private final TemplateRenderService renderService;
    private final ResumeRecordRepository recordRepository;

    public HomeController(TemplateRepository templateRepository, UserRepository userRepository, ResumeService resumeService, TemplateRenderService renderService, ResumeRecordRepository recordRepository) {
        this.templateRepository = templateRepository;
        this.userRepository = userRepository;
        this.resumeService = resumeService;
        this.renderService = renderService;
        this.recordRepository = recordRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<ResumeTemplate> templates = templateRepository.findByActiveTrueOrderByIdAsc();
        model.addAttribute("templates", templates);
        model.addAttribute("templatePreviews", renderService.renderMiniPreviewMap(templates));
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal, Authentication authentication) {
        AppUser user = userRepository.findByUsername(principal.getName()).orElseThrow();
        ResumeProfile profile = resumeService.getOrCreate(user);
        List<ResumeTemplate> templates = templateRepository.findByActiveTrueOrderByIdAsc();
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("templates", templates);
        model.addAttribute("templatePreviews", renderService.renderMiniPreviewMap(templates));
        model.addAttribute("records", recordRepository.findByUserOrderByCreatedAtDesc(user));
        model.addAttribute("recordCount", recordRepository.countByUser(user));
        model.addAttribute("isAdmin", authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        return "dashboard";
    }
}
