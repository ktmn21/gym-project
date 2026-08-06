package com.example.gymcrm.config;

import com.example.gymcrm.logging.TransactionIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<TransactionIdFilter> transactionIdFilter() {
        FilterRegistrationBean<TransactionIdFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new TransactionIdFilter());
        reg.addUrlPatterns("/*");
        return reg;
    }
}