package com.example.gymcrm;

import com.example.gymcrm.config.AppConfig;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Starting Gym CRM Application...");

        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        EntityManagerFactory emf = context.getBean(EntityManagerFactory.class);

        log.info("EntityManagerFactory created: {}", emf != null);

        context.close();
        log.info("Gym CRM Application finished.");
    }
}