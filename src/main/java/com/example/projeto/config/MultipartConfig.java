package com.example.projeto.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
claude --dangerously-skip-permissionsclaude --dangerously-skip-permissions
@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        // location, maxFileSize(10MB), maxRequestSize(10MB), fileSizeThreshold
        return new MultipartConfigElement("", 10 * 1024 * 1024L, 10 * 1024 * 1024L, 0);
    }
}
