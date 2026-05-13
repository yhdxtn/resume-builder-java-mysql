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
        normalizeOldUsers();
        createTemplates();
    }

    private void createAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setFullName("系统管理员");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setPoints(9999);
            userRepository.save(admin);
        }
    }


    private void normalizeOldUsers() {
        userRepository.findAll().forEach(user -> {
            boolean changed = false;
            if (user.getPoints() == null) {
                user.setPoints(100);
                changed = true;
            }
            if (user.getRole() == null || user.getRole().isBlank()) {
                user.setRole("USER");
                changed = true;
            }
            if (changed) {
                userRepository.save(user);
            }
        });
    }

    private void createTemplates() {
        List<SeedTemplate> list = List.of(
                new SeedTemplate("modern-blue", "流光双栏模板", "顶部色块与左右分栏结合，适合大多数岗位。", "#2563eb", "modern"),
                new SeedTemplate("minimal-clean", "白屿极简模板", "留白充足、信息清爽，适合校招、行政、文职。", "#111827", "minimal"),
                new SeedTemplate("tech-dark", "玄夜代码模板", "深色科技风，适合 Java、前端、运维、人工智能方向。", "#38bdf8", "tech"),
                new SeedTemplate("business-side", "靛蓝侧栏模板", "左侧信息栏突出，适合综合管理、运营、项目协调。", "#4f46e5", "dark-side"),
                new SeedTemplate("fresh-campus", "青柠校招模板", "轻快卡片风，适合应届生、实习生和校园招聘。", "#06b6d4", "fresh"),
                new SeedTemplate("admin-ops", "松石事务模板", "突出办公、台账、协调、运维等经历。", "#0f766e", "admin"),
                new SeedTemplate("developer-project", "星链项目模板", "项目经历优先，适合 Java / 前端 / 运维岗位。", "#7c3aed", "developer"),
                new SeedTemplate("finance-data", "琥珀数据模板", "适合财务、金融、数据统计、分析类岗位。", "#b45309", "finance"),
                new SeedTemplate("creative-card", "玫瑰卡片模板", "模块卡片化，更适合展示项目和作品。", "#db2777", "card"),
                new SeedTemplate("classic-formal", "墨线正式模板", "传统正式版式，适合国企、事业单位、正式投递。", "#334155", "classic"),
                new SeedTemplate("architect-line", "星岸蓝线模板", "参考建筑/设计类横线版式，顶部居中信息，模块清楚。", "#4b6f88", "architect-line"),
                new SeedTemplate("midnight-banner", "夜幕弧光模板", "深色顶部横幅与浅色正文结合，适合运营、品牌、商务岗位。", "#171a2a", "midnight-banner"),
                new SeedTemplate("navy-ribbon", "海屿标题模板", "蓝色标签标题样式，正式但更有设计感。", "#183f5b", "navy-ribbon"),
                new SeedTemplate("blue-axis", "青锋时间轴模板", "左侧时间轴感布局，适合经历较多的运营/项目类岗位。", "#1d4ed8", "blue-axis"),
                new SeedTemplate("violet-split", "紫境双栏模板", "紫色顶部名片与左右双栏，适合电商运营、新媒体运营。", "#635bff", "violet-split"),
                new SeedTemplate("amber-board", "暖阳侧栏模板", "黄色信息带与侧栏组合，适合活泼型运营、策划岗位。", "#f4c84a", "amber-board"),
                new SeedTemplate("quiet-gray", "云灰通栏模板", "灰蓝通栏标题，阅读感稳定，适合建筑、行政、文职。", "#31556d", "quiet-gray"),
                new SeedTemplate("mint-profile", "松青清雅模板", "浅绿色条形标题，适合设计、教育、文职类简历。", "#58a6a6", "mint-profile"),
                new SeedTemplate("deep-sidebar", "藏蓝名片模板", "深蓝侧栏名片式版式，适合平面设计、综合岗、管理岗。", "#254766", "deep-sidebar")
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
