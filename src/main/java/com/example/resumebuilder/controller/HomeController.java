package com.example.resumebuilder.controller;

import com.example.resumebuilder.entity.AppUser;
import com.example.resumebuilder.entity.ResumeProfile;
import com.example.resumebuilder.repository.TemplateRepository;
import com.example.resumebuilder.repository.UserRepository;
import com.example.resumebuilder.service.ResumeService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {
    private final TemplateRepository templateRepository;
    private final UserRepository userRepository;
    private final ResumeService resumeService;

    public HomeController(TemplateRepository templateRepository, UserRepository userRepository, ResumeService resumeService) {
        this.templateRepository = templateRepository;
        this.userRepository = userRepository;
        this.resumeService = resumeService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("templates", templateRepository.findByActiveTrueOrderByIdAsc());
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal, Authentication authentication) {
        AppUser user = userRepository.findByUsername(principal.getName()).orElseThrow();
        ResumeProfile profile = resumeService.getOrCreate(user);
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("templates", templateRepository.findByActiveTrueOrderByIdAsc());
        model.addAttribute("isAdmin", authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        return "dashboard";
    }
}
