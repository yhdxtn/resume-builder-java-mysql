package com.example.resumebuilder.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

@Service
public class PdfService {
    public byte[] toPdf(String html) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            registerCjkFont(builder);
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("PDF 生成失败，请检查模板 HTML 是否闭合完整。", e);
        }
    }

    /**
     * 不打包字体文件，避免版权问题；运行时自动尝试使用系统中文字体。
     */
    private void registerCjkFont(PdfRendererBuilder builder) {
        List<String> candidates = List.of(
                "C:/Windows/Fonts/msyh.ttc",
                "C:/Windows/Fonts/simsun.ttc",
                "/System/Library/Fonts/PingFang.ttc",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
                "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc"
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
