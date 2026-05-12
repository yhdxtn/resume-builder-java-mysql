package com.example.resumebuilder.data;

import com.example.resumebuilder.entity.AppUser;
import com.example.resumebuilder.entity.ResumeTemplate;
import com.example.resumebuilder.repository.TemplateRepository;
import com.example.resumebuilder.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, TemplateRepository templateRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.templateRepository = templateRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createAdmin();
        createTemplates();
    }

    private void createAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setFullName("系统管理员");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }
    }

    private void createTemplates() {
        List<SeedTemplate> list = List.of(
                new SeedTemplate("modern-blue", "现代双栏模板", "适合大多数岗位，顶部渐变，信息层次清楚。", "#2563eb", "modern"),
                new SeedTemplate("minimal-clean", "极简白色模板", "适合校招、行政、文职，简洁耐看。", "#111827", "minimal"),
                new SeedTemplate("tech-dark", "科技深色模板", "适合程序员、数据分析、人工智能方向。", "#38bdf8", "tech"),
                new SeedTemplate("business-side", "商务侧栏模板", "适合管理、运营、综合岗，左侧信息栏突出。", "#4f46e5", "dark-side"),
                new SeedTemplate("fresh-campus", "清新校招模板", "适合应届生、实习生，整体更轻快。", "#06b6d4", "fresh"),
                new SeedTemplate("admin-ops", "综合管理岗位模板", "突出办公、台账、协调、运维等经历。", "#0f766e", "admin"),
                new SeedTemplate("developer-project", "程序员项目型模板", "项目经历优先，适合 Java / 前端 / 运维岗位。", "#7c3aed", "developer"),
                new SeedTemplate("finance-data", "金融数据模板", "适合财务、金融、数据统计、分析类岗位。", "#b45309", "finance"),
                new SeedTemplate("creative-card", "创意卡片模板", "模块卡片化，更适合展示项目和作品。", "#db2777", "card"),
                new SeedTemplate("classic-formal", "经典正式模板", "传统简历风格，适合国企、事业单位、正式投递。", "#334155", "classic")
        );

        for (SeedTemplate seed : list) {
            ResumeTemplate template = templateRepository.findByTemplateKey(seed.key()).orElseGet(() -> {
                ResumeTemplate t = new ResumeTemplate();
                t.setTemplateKey(seed.key());
                return t;
            });
            template.setName(seed.name());
            template.setDescription(seed.description());
            template.setAccentColor(seed.color());
            // 老版本默认模板没有 section/avatar 占位符。这里自动升级默认模板，保证“可选模块”和头像能生效。
            if (template.getHtmlContent() == null || !template.getHtmlContent().contains("{{summarySection}}") || !template.getHtmlContent().contains("{{avatarBlock}}")) {
                template.setHtmlContent(DefaultTemplateLibrary.html(seed.theme()));
            }
            template.setActive(true);
            templateRepository.save(template);
        }
    }

    private record SeedTemplate(String key, String name, String description, String color, String theme) {}
}
