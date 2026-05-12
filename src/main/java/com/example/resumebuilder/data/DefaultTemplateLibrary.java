package com.example.resumebuilder.data;

public final class DefaultTemplateLibrary {
    private DefaultTemplateLibrary() {}

    public static String html(String theme) {
        return switch (theme) {
            case "minimal" -> minimal();
            case "tech" -> tech();
            case "dark-side" -> darkSide();
            case "fresh" -> fresh();
            case "admin" -> admin();
            case "developer" -> developer();
            case "finance" -> finance();
            case "card" -> card();
            case "classic" -> classic();
            default -> modern();
        };
    }

    private static String baseStyle() {
        return """
        @page { size: A4; margin: 0; }
        * { box-sizing: border-box; }
        body { margin: 0; font-family: AppCJK, Arial, sans-serif; color: #1f2937; background: #f1f5f9; }
        .page { width: 210mm; min-height: 297mm; margin: 0 auto; background: #fff; overflow: hidden; }
        h1, h2, h3, p { margin: 0; }
        table { width: 100%; border-collapse: collapse; }
        td { vertical-align: top; }
        .muted { color: #64748b; }
        .section-title { font-size: 15px; letter-spacing: 1px; color: {{accentColor}}; margin: 18px 0 8px; font-weight: 800; }
        .text { font-size: 12px; line-height: 1.8; word-break: break-word; }
        .small { font-size: 11px; line-height: 1.7; }
        .resume-section { margin-bottom: 10px; }
        .avatar-wrap { float: right; width: 76px; height: 76px; border-radius: 50%; overflow: hidden; border: 3px solid rgba(255,255,255,.85); background: rgba(255,255,255,.28); }
        .avatar { width: 100%; height: 100%; object-fit: cover; }
        """;
    }

    private static String wrap(String css, String body) {
        return """
        <!DOCTYPE html>
        <html><head><meta charset="UTF-8"/><style>
        /*BASE*/
        /*CSS*/
        </style></head><body><div class="page">
        /*BODY*/
        </div></body></html>
        """.replace("/*BASE*/", baseStyle()).replace("/*CSS*/", css).replace("/*BODY*/", body);
    }

    private static String modern() {
        return wrap("""
        .hero { padding: 34px 40px 28px; background: {{accentColor}}; color: white; min-height: 130px; }
        .hero h1 { font-size: 34px; margin-bottom: 8px; }
        .hero .job { font-size: 15px; opacity: .95; }
        .contact { margin-top: 18px; font-size: 11px; opacity: .95; }
        .content { padding: 28px 38px; }
        .main { width: 68%; padding-right: 22px; }
        .side { width: 32%; border-left: 2px solid #e5e7eb; padding-left: 20px; }
        .resume-section:first-child .section-title { margin-top: 0; }
        """, """
        <div class="hero">{{avatarBlock}}<h1>{{fullName}}</h1><div class="job">{{jobTitle}}</div><div class="contact">{{contactLine}}</div></div>
        <div class="content"><table><tr><td class="main">
        {{summarySection}}
        {{experienceSection}}
        {{projectsSection}}
        </td><td class="side">
        {{educationSection}}
        {{skillsSection}}
        {{certificatesSection}}
        {{awardsSection}}
        </td></tr></table></div>
        """);
    }

    private static String minimal() {
        return wrap("""
        .page { padding: 38px 46px; }
        .top { border-bottom: 3px solid {{accentColor}}; padding-bottom: 16px; min-height: 92px; }
        h1 { font-size: 34px; color: #111827; }
        .job { margin-top: 6px; color: #475569; }
        .contact { margin-top: 10px; font-size: 11px; color: #64748b; }
        .section-title { color: #111827; border-bottom: 1px solid #e5e7eb; padding-bottom: 5px; }
        .avatar-wrap { border-color: #e5e7eb; }
        """, """
        <div class="top">{{avatarBlock}}<h1>{{fullName}}</h1><div class="job">{{jobTitle}}</div><div class="contact">{{contactLine}}</div></div>
        {{summarySection}}
        {{educationSection}}
        {{experienceSection}}
        {{projectsSection}}
        {{skillsSection}}
        {{certificatesSection}}
        {{awardsSection}}
        """);
    }

    private static String tech() {
        return wrap("""
        .page { background: #0f172a; color: #e5e7eb; }
        .head { padding: 34px 42px; background: #111827; border-bottom: 6px solid {{accentColor}}; min-height: 132px; }
        h1 { font-size: 34px; color: #fff; }
        .job { color: #a5b4fc; margin-top: 6px; }
        .contact { margin-top: 14px; font-size: 11px; color: #cbd5e1; }
        .wrap { padding: 26px 42px; }
        .col { width: 50%; padding: 8px; }
        .resume-section { background: #182235; border: 1px solid #334155; padding: 14px; margin-bottom: 12px; }
        .section-title { color: #93c5fd; margin-top: 0; }
        .text { color: #e5e7eb; }
        """, """
        <div class="head">{{avatarBlock}}<h1>{{fullName}}</h1><div class="job">// {{jobTitle}}</div><div class="contact">{{contactDot}}</div></div>
        <div class="wrap"><table><tr><td class="col">
        {{summarySection}}
        {{projectsSection}}
        {{educationSection}}
        {{certificatesSection}}
        </td><td class="col">
        {{skillsSection}}
        {{experienceSection}}
        {{awardsSection}}
        </td></tr></table></div>
        """);
    }

    private static String darkSide() {
        return wrap("""
        .left { width: 34%; background: #111827; color: #fff; padding: 36px 24px; min-height: 297mm; }
        .right { width: 66%; padding: 36px 34px; }
        h1 { font-size: 30px; line-height: 1.2; }
        .job { color: #c7d2fe; margin: 12px 0 20px; }
        .left .section-title { color: #fff; border-bottom: 1px solid #374151; padding-bottom: 6px; }
        .left .text { color: #e5e7eb; }
        .right .section-title { color: {{accentColor}}; }
        .contact { font-size: 12px; line-height: 1.8; color: #e5e7eb; }
        .avatar-wrap { float: none; margin-bottom: 18px; width: 88px; height: 88px; }
        """, """
        <table><tr><td class="left">{{avatarBlock}}<h1>{{fullName}}</h1><div class="job">{{jobTitle}}</div>
        <div class="section-title">联系方式</div><div class="contact">{{contactStack}}</div>
        {{skillsSection}}
        {{certificatesSection}}
        {{awardsSection}}
        </td><td class="right">
        {{summarySection}}
        {{educationSection}}
        {{experienceSection}}
        {{projectsSection}}
        </td></tr></table>
        """);
    }

    private static String fresh() {
        return wrap("""
        .page { padding: 36px 42px; background: #f0fdfa; }
        .top { background: #fff; border: 1px solid #ccfbf1; padding: 22px; border-radius: 20px; min-height: 112px; }
        h1 { font-size: 32px; color: #0f172a; }
        .badge { background: {{accentColor}}; color: #fff; padding: 8px 12px; border-radius: 12px; font-size: 12px; float: right; margin-left: 12px; }
        .contact { margin-top: 12px; color: #64748b; font-size: 11px; }
        .section-title { color: #0891b2; margin-top: 0; }
        .resume-section { border: 1px solid #bae6fd; background: #fff; border-radius: 15px; padding: 14px 16px; margin-top: 12px; }
        .avatar-wrap { border-color: #ccfbf1; }
        """, """
        <div class="top">{{avatarBlock}}<div class="badge">{{jobTitle}}</div><h1>{{fullName}}</h1><div class="contact">{{contactDot}}</div></div>
        {{summarySection}}
        {{educationSection}}
        {{experienceSection}}
        {{projectsSection}}
        {{skillsSection}}
        {{certificatesSection}}
        {{awardsSection}}
        """);
    }

    private static String admin() {
        return wrap("""
        .page { padding: 34px 40px; }
        .top { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 20px; padding: 22px; min-height: 112px; }
        h1 { font-size: 31px; color: #111827; }
        .job { color: {{accentColor}}; margin-top: 7px; font-weight: bold; }
        .contact { margin-top: 10px; color: #475569; font-size: 11px; }
        .section-title { color: #0f172a; border-bottom: 2px solid {{accentColor}}; padding-bottom: 5px; }
        .avatar-wrap { border-color: #e2e8f0; }
        """, """
        <div class="top">{{avatarBlock}}<h1>{{fullName}}</h1><div class="job">{{jobTitle}}</div><div class="contact">{{contactLine}}</div></div>
        {{summarySection}}
        {{experienceSection}}
        {{projectsSection}}
        {{educationSection}}
        {{skillsSection}}
        {{certificatesSection}}
        {{awardsSection}}
        """);
    }

    private static String developer() {
        return wrap("""
        .page { padding: 32px 40px; background: #fff; }
        .top { border-left: 10px solid {{accentColor}}; padding-left: 18px; min-height: 96px; }
        h1 { font-size: 34px; }
        .job { margin-top: 6px; font-family: monospace; color: {{accentColor}}; }
        .contact { margin-top: 11px; font-size: 11px; color: #64748b; }
        .section-title { color: {{accentColor}}; }
        .skills-code .text { background: #0f172a; color: #d1d5db; border-radius: 14px; padding: 13px 15px; font-family: AppCJK, monospace; }
        .avatar-wrap { border-color: #e5e7eb; }
        """, """
        <div class="top">{{avatarBlock}}<h1>{{fullName}}</h1><div class="job">// {{jobTitle}}</div><div class="contact">{{contactDot}}</div></div>
        <div class="skills-code">{{skillsSection}}</div>
        {{projectsSection}}
        {{experienceSection}}
        {{summarySection}}
        {{educationSection}}
        {{certificatesSection}}
        {{awardsSection}}
        """);
    }

    private static String finance() {
        return wrap("""
        .page { padding: 38px 44px; }
        .top { text-align: center; border-top: 5px solid {{accentColor}}; border-bottom: 1px solid #e5e7eb; padding: 20px 0; min-height: 112px; }
        h1 { font-size: 32px; }
        .job { margin-top: 6px; color: {{accentColor}}; font-weight: 700; }
        .contact { margin-top: 9px; font-size: 11px; color: #64748b; }
        .section-title { color: #111827; text-transform: uppercase; }
        .section-title:after { content: ''; display: block; height: 2px; background: {{accentColor}}; width: 54px; margin-top: 5px; }
        .avatar-wrap { float: none; margin: 0 auto 12px; border-color: #e5e7eb; }
        """, """
        <div class="top">{{avatarBlock}}<h1>{{fullName}}</h1><div class="job">{{jobTitle}}</div><div class="contact">{{contactLine}}</div></div>
        {{summarySection}}
        {{experienceSection}}
        {{projectsSection}}
        {{educationSection}}
        {{skillsSection}}
        {{certificatesSection}}
        {{awardsSection}}
        """);
    }

    private static String card() {
        return wrap("""
        .page { padding: 34px 38px; background: #f8fafc; }
        .hero { background: #fff; border-radius: 24px; padding: 24px; border-top: 8px solid {{accentColor}}; min-height: 118px; }
        h1 { font-size: 32px; }
        .job { color: {{accentColor}}; margin-top: 6px; font-weight: bold; }
        .contact { margin-top: 10px; font-size: 11px; color: #64748b; }
        .grid { margin-top: 16px; }
        .col { width: 50%; padding: 6px; }
        .resume-section { background: #fff; border: 1px solid #e2e8f0; border-radius: 18px; padding: 14px; margin-bottom: 12px; }
        .section-title { margin-top: 0; }
        .avatar-wrap { border-color: #e2e8f0; }
        """, """
        <div class="hero">{{avatarBlock}}<h1>{{fullName}}</h1><div class="job">{{jobTitle}}</div><div class="contact">{{contactDot}}</div></div>
        <div class="grid"><table><tr><td class="col">
        {{summarySection}}
        {{educationSection}}
        {{skillsSection}}
        {{certificatesSection}}
        </td><td class="col">
        {{experienceSection}}
        {{projectsSection}}
        {{awardsSection}}
        </td></tr></table></div>
        """);
    }

    private static String classic() {
        return wrap("""
        .page { padding: 36px 42px; }
        h1 { font-size: 30px; text-align: center; color: #111827; }
        .job { text-align: center; margin-top: 6px; color: {{accentColor}}; }
        .contact { text-align: center; margin-top: 10px; font-size: 11px; color: #64748b; }
        .line { height: 2px; background: #111827; margin: 18px 0; }
        .section-title { color: #111827; font-size: 14px; border-bottom: 1px solid #cbd5e1; padding-bottom: 4px; }
        .avatar-center { text-align: center; }
        .avatar-center .avatar-wrap { float: none; display: inline-block; border-color: #e5e7eb; margin-bottom: 12px; }
        """, """
        <div class="avatar-center">{{avatarBlock}}</div><h1>{{fullName}}</h1><div class="job">{{jobTitle}}</div><div class="contact">{{contactLine}}</div><div class="line"></div>
        {{summarySection}}
        {{educationSection}}
        {{experienceSection}}
        {{projectsSection}}
        {{skillsSection}}
        {{certificatesSection}}
        {{awardsSection}}
        """);
    }
}
