package com.example.gymcrm.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.example.gymcrm")
public class AppConfig {

    @Bean(destroyMethod = "close")
    public EntityManagerFactory entityManagerFactory(){
        return Persistence.createEntityManagerFactory("gymcrm");
    }

}
