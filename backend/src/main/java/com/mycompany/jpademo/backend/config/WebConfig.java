package com.mycompany.jpademo.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers static pages that don't need a dedicated Controller (pure view
 * rendering, no logic) — including two pages that belong to the Google
 * login flow: the main login page and the first-time "complete your
 * profile" page.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/auth/login").setViewName("auth/login");
        registry.addViewController("/auth/google-complete").setViewName("auth/google-complete");
        registry.addRedirectViewController("/", "/auth/login");
    }
}