package com.pastagem.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.pastagem.security.CognitoTokenValidationInterceptor;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private CognitoTokenValidationInterceptor cognitoTokenValidationInterceptor;

    @SuppressWarnings("null")
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cognitoTokenValidationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/usuarios/register");
    }
} 