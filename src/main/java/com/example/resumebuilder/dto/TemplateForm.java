package com.example.resumebuilder.dto;

import jakarta.validation.constraints.NotBlank;

public class TemplateForm {
    private Long id;

    @NotBlank(message = "模板编码不能为空")
    private String templateKey;

    @NotBlank(message = "模板名称不能为空")
    private String name;

    private String description;
    private String accentColor = "#2563eb";

    @NotBlank(message = "模板 HTML 不能为空")
    private String htmlContent;

    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTemplateKey() { return templateKey; }
    public void setTemplateKey(String templateKey) { this.templateKey = templateKey; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAccentColor() { return accentColor; }
    public void setAccentColor(String accentColor) { this.accentColor = accentColor; }
    public String getHtmlContent() { return htmlContent; }
    public void setHtmlContent(String htmlContent) { this.htmlContent = htmlContent; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
