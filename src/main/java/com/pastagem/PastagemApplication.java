package com.pastagem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.pastagem.repository")
public class PastagemApplication {
    public static void main(String[] args) {
        SpringApplication.run(PastagemApplication.class, args);
    }
} 