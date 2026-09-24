package com.example.gymcrm.cucumber.config;

import com.example.gymcrm.GymCrmApplication;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@CucumberContextConfiguration
@SpringBootTest(classes = GymCrmApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {

    static final WireMockServer WORKLOAD_MOCK =
            new WireMockServer(WireMockConfiguration.options().dynamicPort());

    static {
        WORKLOAD_MOCK.start();
    }

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("workload.service.url", WORKLOAD_MOCK::baseUrl);
    }

    @jakarta.annotation.PostConstruct
    void configureRestAssured() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @PreDestroy
    void stopWireMock() {
        if (WORKLOAD_MOCK.isRunning()) {
            WORKLOAD_MOCK.stop();
        }
    }

    public static WireMockServer workloadMock() {
        return WORKLOAD_MOCK;
    }
}
