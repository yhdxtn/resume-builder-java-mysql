package com.example.resumebuilder.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PdfService {
    public byte[] toPdf(String html) {
        String safeHtml = normalizeHtml(html);
        String pdfHtml = prepareHtmlForPdf(safeHtml);
        try {
            return renderToPdf(pdfHtml);
        } catch (Exception first) {
            try {
                // 个别头像图片格式异常时，保留模板版式，只移除图片再试一次，避免退化成纯文本 PDF。
                return renderToPdf(removeImages(pdfHtml));
            } catch (Exception second) {
                try {
                    // 最后兜底仍然生成一份简历 PDF，但正常情况下不会走到这里。
                    return renderToPdf(prepareHtmlForPdf(fallbackHtml(safeHtml)));
                } catch (Exception third) {
                    throw new IllegalStateException("PDF 生成失败，请检查模板 HTML 是否闭合完整。", third);
                }
            }
        }
    }

    private byte[] renderToPdf(String html) throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            registerCjkFont(builder);
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        }
    }

    private String normalizeHtml(String html) {
        if (html == null || html.isBlank()) {
            return fallbackHtml("简历内容为空");
        }
        String cleaned = html.replace("\u0000", "");
        cleaned = cleaned.replaceAll("(?is)<script[^>]*>.*?</script>", "");
        // openhtmltopdf / PDFBox 不稳定支持 WebP，这里主动移除 WebP，避免整份 PDF 退化。
        cleaned = cleaned.replaceAll("(?i)src=\"data:image/webp;base64,[^\"]+\"", "src=\"\"");
        cleaned = cleaned.replaceAll("(?i)src='data:image/webp;base64,[^']+'", "src='' ");
        return cleaned;
    }

    /**
     * 页面预览用的是浏览器渲染，PDF 用 openhtmltopdf 渲染。
     * openhtmltopdf 对部分浏览器 CSS、HTML5 松散写法支持不完整，所以这里把 HTML 规范化为 XHTML，
     * 同时去掉容易导致 PDF 渲染失败的浏览器专用样式。这样 PDF 会继续使用同一份模板结构，
     * 不会再变成截图里那种纯文本 PDF。
     */
    private String prepareHtmlForPdf(String html) {
        String cssSafe = sanitizeUnsupportedCss(html);
        Document doc = Jsoup.parse(cssSafe);
        doc.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)
                .escapeMode(Entities.EscapeMode.xhtml)
                .charset("UTF-8")
                .prettyPrint(false);
        return doc.html();
    }

    private String sanitizeUnsupportedCss(String html) {
        String result = html;
        result = removeCssMediaBlocks(result);
        result = result.replaceAll("(?is)/\\*.*?\\*/", "");
        // openhtmltopdf 不支持渐变、阴影、transform 等浏览器效果；保留核心布局、颜色、字号和边距。
        result = result.replaceAll("(?i)background\\s*:\\s*linear-gradient\\([^;]+;", "background: #f8fafc;");
        result = result.replaceAll("(?i)background-image\\s*:\\s*linear-gradient\\([^;]+;", "background-image: none;");
        result = result.replaceAll("(?i)box-shadow\\s*:[^;]+;", "");
        result = result.replaceAll("(?i)text-shadow\\s*:[^;]+;", "");
        result = result.replaceAll("(?i)transform\\s*:[^;]+;", "");
        result = result.replaceAll("(?i)transition\\s*:[^;]+;", "");
        result = result.replaceAll("(?i)filter\\s*:[^;]+;", "");
        result = result.replaceAll("(?i)object-fit\\s*:[^;]+;", "");
        result = result.replaceAll("(?i)gap\\s*:[^;]+;", "");
        result = result.replaceAll("(?i)display\\s*:\\s*grid\\s*;", "display: block;");
        result = result.replaceAll("(?i)display\\s*:\\s*flex\\s*;", "display: block;");
        result = result.replaceAll("(?i)align-items\\s*:[^;]+;", "");
        result = result.replaceAll("(?i)justify-content\\s*:[^;]+;", "");
        return result;
    }

    private String removeCssMediaBlocks(String html) {
        String marker = "@media";
        int index = html.toLowerCase().indexOf(marker);
        while (index >= 0) {
            int openBrace = html.indexOf('{', index);
            if (openBrace < 0) break;
            int depth = 0;
            int end = -1;
            for (int i = openBrace; i < html.length(); i++) {
                char c = html.charAt(i);
                if (c == '{') depth++;
                if (c == '}') depth--;
                if (depth == 0) {
                    end = i;
                    break;
                }
            }
            if (end < 0) break;
            html = html.substring(0, index) + html.substring(end + 1);
            index = html.toLowerCase().indexOf(marker);
        }
        return html;
    }

    private String removeImages(String html) {
        return Pattern.compile("(?is)<img[^>]*>").matcher(html).replaceAll("");
    }

    private String fallbackHtml(String html) {
        String text = html == null ? "简历内容为空" : html
                .replaceAll("(?is)<style[^>]*>.*?</style>", " ")
                .replaceAll("(?is)<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (text.isBlank()) {
            text = "简历内容为空";
        }
        return """
                <!DOCTYPE html>
                <html><head><meta charset="UTF-8"/><style>
                @page { size: A4; margin: 18mm; }
                body { font-family: AppCJK, Arial, sans-serif; color: #111827; line-height: 1.8; font-size: 12px; }
                h1 { font-size: 24px; margin: 0 0 16px; color: #2563eb; }
                .box { white-space: pre-wrap; word-break: break-word; }
                </style></head><body><h1>个人简历</h1><div class="box">%s</div></body></html>
                """.formatted(HtmlUtils.htmlEscape(text));
    }

    /**
     * 不打包字体文件，避免版权问题；运行时自动尝试使用系统中文字体。
     */
    private void registerCjkFont(PdfRendererBuilder builder) {
        List<String> candidates = List.of(
                "C:/Windows/Fonts/msyh.ttc",
                "C:/Windows/Fonts/msyh.ttf",
                "C:/Windows/Fonts/simsun.ttc",
                "C:/Windows/Fonts/simsun.ttf",
                "/System/Library/Fonts/PingFang.ttc",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
                "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc",
                "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc"
        );
        for (String p : candidates) {
            File file = Path.of(p).toFile();
            if (file.exists()) {
                builder.useFont(file, "AppCJK");
                return;
            }
        }
    }
}
