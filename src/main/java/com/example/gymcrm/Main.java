package com.example.gymcrm;

import com.example.gymcrm.config.AppConfig;
import com.example.gymcrm.config.PersistenceConfig;
import com.example.gymcrm.config.SpringDocConfig;
import com.example.gymcrm.config.WebConfig;
import com.example.gymcrm.logging.TransactionIdFilter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class Main extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] {AppConfig.class, PersistenceConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] {WebConfig.class, SpringDocConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);

        FilterRegistration.Dynamic transactionIdFilter =
                servletContext.addFilter("transactionIdFilter", new TransactionIdFilter());
        transactionIdFilter.addMappingForUrlPatterns(null, false, "/*");

        FilterRegistration.Dynamic encodingFilter =
                servletContext.addFilter("encoding-filter", new CharacterEncodingFilter());
        encodingFilter.setInitParameter("encoding", "UTF-8");
        encodingFilter.setInitParameter("forceEncoding", "true");
        encodingFilter.addMappingForUrlPatterns(null, true, "/*");
    }
}