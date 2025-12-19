package com.e_commerce.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve files under uploads/avatars via /avatars/** URL path
        java.nio.file.Path path = java.nio.file.Paths.get("uploads", "avatars").toAbsolutePath();
        String location = "file:" + path.toString() + java.io.File.separator;
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations(location);
    }
}
