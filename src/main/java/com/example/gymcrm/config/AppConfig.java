package com.example.gymcrm.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@Import(PersistenceConfig.class)
@ComponentScan(basePackages = {"com.example.gymcrm.service",
                                "com.example.gymcrm.dao",
                                "com.example.gymcrm.util"})
@EnableTransactionManagement
public class AppConfig {

}