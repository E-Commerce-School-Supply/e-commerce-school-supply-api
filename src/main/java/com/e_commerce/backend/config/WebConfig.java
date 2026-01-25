package com.e_commerce.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. handler for avatars
        Path avatarPath = Paths.get("uploads", "avatars").toAbsolutePath();
        String avatarLocation = "file:" + avatarPath.toString() + File.separator;
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations(avatarLocation);

        // 2. handler for product images
        Path productPath = Paths.get("uploads", "products").toAbsolutePath();
        String productLocation = "file:" + productPath.toString() + File.separator;
        registry.addResourceHandler("/products/**")
                .addResourceLocations(productLocation);
    }
}