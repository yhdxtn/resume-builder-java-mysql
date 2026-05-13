package com.example.resumebuilder.controller;

import com.example.resumebuilder.dto.TemplateForm;
import com.example.resumebuilder.entity.ResumeTemplate;
import com.example.resumebuilder.repository.TemplateRepository;
import com.example.resumebuilder.service.TemplateRenderService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/templates")
public class AdminTemplateController {
    private final TemplateRepository templateRepository;
    private final TemplateRenderService renderService;

    public AdminTemplateController(TemplateRepository templateRepository, TemplateRenderService renderService) {
        this.templateRepository = templateRepository;
        this.renderService = renderService;
    }

    @GetMapping
    public String list(Model model) {
        List<ResumeTemplate> templates = templateRepository.findAllByOrderByIdAsc();
        model.addAttribute("templates", templates);
        model.addAttribute("templatePreviews", renderService.renderMiniPreviewMap(templates));
        return "admin/templates/list";
    }

    @GetMapping("/new")
    public String createPage(Model model) {
        TemplateForm form = new TemplateForm();
        form.setAccentColor("#2563eb");
        form.setHtmlContent(defaultBlankTemplate());
        model.addAttribute("form", form);
        model.addAttribute("mode", "new");
        return "admin/templates/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("form") TemplateForm form, BindingResult bindingResult, Model model) {
        if (templateRepository.findByTemplateKey(form.getTemplateKey()).isPresent()) {
            bindingResult.rejectValue("templateKey", "exists", "模板编码已存在");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "new");
            return "admin/templates/form";
        }
        ResumeTemplate t = new ResumeTemplate();
        copy(form, t);
        templateRepository.save(t);
        return "redirect:/admin/templates";
    }

    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, Model model) {
        ResumeTemplate t = templateRepository.findById(id).orElseThrow();
        model.addAttribute("form", toForm(t));
        model.addAttribute("mode", "edit");
        model.addAttribute("previewHtml", renderService.renderMiniPreview(t));
        return "admin/templates/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @Valid @ModelAttribute("form") TemplateForm form, BindingResult bindingResult, Model model) {
        ResumeTemplate t = templateRepository.findById(id).orElseThrow();
        templateRepository.findByTemplateKey(form.getTemplateKey()).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                bindingResult.rejectValue("templateKey", "exists", "模板编码已存在");
            }
        });
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "edit");
            return "admin/templates/form";
        }
        copy(form, t);
        templateRepository.save(t);
        return "redirect:/admin/templates";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id) {
        ResumeTemplate t = templateRepository.findById(id).orElseThrow();
        t.setActive(!t.isActive());
        templateRepository.save(t);
        return "redirect:/admin/templates";
    }

    private void copy(TemplateForm form, ResumeTemplate t) {
        t.setTemplateKey(form.getTemplateKey().trim());
        t.setName(form.getName().trim());
        t.setDescription(form.getDescription());
        t.setAccentColor(form.getAccentColor());
        t.setHtmlContent(form.getHtmlContent());
        t.setActive(form.isActive());
    }

    private TemplateForm toForm(ResumeTemplate t) {
        TemplateForm form = new TemplateForm();
        form.setId(t.getId());
        form.setTemplateKey(t.getTemplateKey());
        form.setName(t.getName());
        form.setDescription(t.getDescription());
        form.setAccentColor(t.getAccentColor());
        form.setHtmlContent(t.getHtmlContent());
        form.setActive(t.isActive());
        return form;
    }

    private String defaultBlankTemplate() {
        return """
        <!DOCTYPE html>
        <html><head><meta charset="UTF-8"/><style>
        @page { size: A4; margin: 0; }
        * { box-sizing: border-box; }
        body { margin: 0; font-family: AppCJK, Arial, sans-serif; color: #1f2937; }
        .page { width: 210mm; min-height: 297mm; padding: 38px 44px; background: #fff; }
        h1 { margin: 0; font-size: 32px; }
        .accent { color: {{accentColor}}; }
        .section { margin-top: 18px; }
        .title { font-weight: 800; color: {{accentColor}}; border-bottom: 2px solid {{accentColor}}; padding-bottom: 5px; }
        .text { margin-top: 8px; font-size: 12px; line-height: 1.8; }
        </style></head><body><div class="page">
        {{avatarBlock}}
        <h1>{{fullName}}</h1>
        <div class="accent">{{jobTitle}}</div>
        <div class="text">{{contactLine}}</div>
        {{summarySection}}
        {{educationSection}}
        {{experienceSection}}
        {{projectsSection}}
        {{skillsSection}}
        {{certificatesSection}}
        {{awardsSection}}
        </div></body></html>
        """;
    }
}
