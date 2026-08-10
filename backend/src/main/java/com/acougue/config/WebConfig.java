package com.acougue.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // CORS gerenciado pelo SecurityConfig (CorsConfigurationSource bean)
}
