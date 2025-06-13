package com.pastagem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Permitir requisições do frontend e do IP local
        config.addAllowedOrigin("http://localhost:3001");
        config.addAllowedOrigin("http://192.168.2.199:3001");
        config.addAllowedOrigin("http://localhost:5174"); // Frontend Vite
        config.addAllowedOrigin("http://127.0.0.1:5174"); // Frontend Vite (alternativo)
        
        // Permitir todos os métodos HTTP
        config.addAllowedMethod("*");
        
        // Permitir todos os headers
        config.addAllowedHeader("*");
        
        // Permitir credenciais (cookies, headers de autenticação)
        config.setAllowCredentials(true);
        
        // Aplicar a configuração para todas as rotas
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
} 