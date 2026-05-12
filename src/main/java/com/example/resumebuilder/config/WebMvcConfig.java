package com.example.resumebuilder.config;

import com.example.resumebuilder.service.AvatarStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final AvatarStorageService avatarStorageService;

    public WebMvcConfig(AvatarStorageService avatarStorageService) {
        this.avatarStorageService = avatarStorageService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(avatarStorageService.getUploadRoot().toUri().toString());
    }
}
