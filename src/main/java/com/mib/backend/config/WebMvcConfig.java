package com.mib.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final StorageProperties storageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Path.of(storageProperties.getAvatarDir()).toAbsolutePath().normalize();

        registry.addResourceHandler(storageProperties.getAvatarPublicPath() + "/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
