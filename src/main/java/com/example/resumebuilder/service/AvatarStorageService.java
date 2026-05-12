package com.example.resumebuilder.service;

import com.example.resumebuilder.entity.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AvatarStorageService {
    private final Path uploadRoot;

    public AvatarStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String storeAvatar(AppUser user, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.startsWith("image/")) {
            throw new IllegalArgumentException("头像只能上传图片文件。支持 JPG、PNG、WebP 等常见格式。");
        }
        try {
            Files.createDirectories(uploadRoot.resolve("avatars"));
            String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "avatar" : file.getOriginalFilename());
            String extension = extensionOf(original, contentType);
            String fileName = "user-" + user.getId() + "-" + UUID.randomUUID() + extension;
            Path target = uploadRoot.resolve("avatars").resolve(fileName).normalize();
            if (!target.startsWith(uploadRoot)) {
                throw new IllegalArgumentException("文件路径不合法。请重新选择头像。 ");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/avatars/" + fileName;
        } catch (IOException e) {
            throw new IllegalStateException("头像上传失败，请稍后重试。", e);
        }
    }

    public String toDataUri(String publicPath) {
        Optional<Path> path = resolve(publicPath);
        if (path.isEmpty()) {
            return "";
        }
        try {
            byte[] bytes = Files.readAllBytes(path.get());
            String mime = Files.probeContentType(path.get());
            if (mime == null || mime.isBlank()) {
                mime = "image/jpeg";
            }
            return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            return "";
        }
    }

    public Optional<Path> resolve(String publicPath) {
        if (publicPath == null || publicPath.isBlank() || !publicPath.startsWith("/uploads/")) {
            return Optional.empty();
        }
        String relative = publicPath.substring("/uploads/".length());
        Path path = uploadRoot.resolve(relative).normalize();
        if (!path.startsWith(uploadRoot) || !Files.exists(path)) {
            return Optional.empty();
        }
        return Optional.of(path);
    }

    public Path getUploadRoot() {
        return uploadRoot;
    }

    private String extensionOf(String original, String contentType) {
        String lower = original.toLowerCase(Locale.ROOT);
        int dot = lower.lastIndexOf('.');
        if (dot >= 0 && dot < lower.length() - 1) {
            String ext = lower.substring(dot);
            if (ext.matches("\\.(jpg|jpeg|png|gif|webp|bmp)$")) {
                return ext;
            }
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/bmp" -> ".bmp";
            default -> ".jpg";
        };
    }
}
