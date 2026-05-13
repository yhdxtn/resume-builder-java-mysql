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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "avatar" : file.getOriginalFilename());
            return storeBytes(user, file.getBytes(), extensionOf(original, contentType));
        } catch (IOException e) {
            throw new IllegalStateException("头像上传失败，请稍后重试。", e);
        }
    }

    /**
     * 保存前端裁切后的头像 data URI。没有裁切数据时返回 null。
     */
    public String storeDataUriAvatar(AppUser user, String dataUri) {
        if (dataUri == null || dataUri.isBlank()) {
            return null;
        }
        String trimmed = dataUri.trim();
        if (trimmed.length() > 3_000_000) {
            throw new IllegalArgumentException("头像图片太大，请裁切或压缩后再上传。");
        }
        Pattern pattern = Pattern.compile("^data:(image/(png|jpeg|jpg|webp|gif|bmp));base64,(.+)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(trimmed);
        if (!matcher.matches()) {
            return null;
        }
        String mime = matcher.group(1).toLowerCase(Locale.ROOT);
        String payload = matcher.group(3).replaceAll("\\s", "");
        try {
            byte[] bytes = Base64.getDecoder().decode(payload);
            if (bytes.length > 2_500_000) {
                throw new IllegalArgumentException("头像图片太大，请裁切或压缩后再上传。");
            }
            return storeBytes(user, bytes, extensionByMime(mime));
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("头像图片太大")) {
                throw e;
            }
            throw new IllegalArgumentException("头像裁切数据无效，请重新选择图片。");
        } catch (IOException e) {
            throw new IllegalStateException("头像保存失败，请稍后重试。", e);
        }
    }

    private String storeBytes(AppUser user, byte[] bytes, String extension) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        Files.createDirectories(uploadRoot.resolve("avatars"));
        String fileName = "user-" + user.getId() + "-" + UUID.randomUUID() + extension;
        Path target = uploadRoot.resolve("avatars").resolve(fileName).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("文件路径不合法。请重新选择头像。 ");
        }
        Files.write(target, bytes);
        return "/uploads/avatars/" + fileName;
    }

    public String toDataUri(String publicPath) {
        if (publicPath == null || publicPath.isBlank()) {
            return "";
        }
        String trimmed = publicPath.trim();
        if (trimmed.toLowerCase(Locale.ROOT).startsWith("data:image/") && trimmed.contains(";base64,")) {
            return trimmed;
        }
        Optional<Path> path = resolve(trimmed);
        if (path.isEmpty()) {
            return "";
        }
        try {
            byte[] bytes = Files.readAllBytes(path.get());
            String mime = Files.probeContentType(path.get());
            if (mime == null || mime.isBlank()) {
                mime = mimeByExtension(path.get().getFileName().toString());
            }
            return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            return "";
        }
    }

    public Optional<Path> resolve(String publicPath) {
        if (publicPath == null || publicPath.isBlank()) {
            return Optional.empty();
        }
        String normalized = publicPath.trim().replace('\\', '/');
        String relative;
        if (normalized.startsWith("/uploads/")) {
            relative = normalized.substring("/uploads/".length());
        } else if (normalized.startsWith("uploads/")) {
            relative = normalized.substring("uploads/".length());
        } else {
            return Optional.empty();
        }
        Path path = uploadRoot.resolve(relative).normalize();
        if (!path.startsWith(uploadRoot) || !Files.exists(path)) {
            return Optional.empty();
        }
        return Optional.of(path);
    }

    public Path getUploadRoot() {
        return uploadRoot;
    }

    private String mimeByExtension(String fileName) {
        String lower = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".bmp")) return "image/bmp";
        return "image/jpeg";
    }

    private String extensionByMime(String mime) {
        return switch (mime) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/bmp" -> ".bmp";
            default -> ".jpg";
        };
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
