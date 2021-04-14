package com.udacity.pricing;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;

/*
RestTemplate configuration file that is used in PricingServiceApplicationTests.java
 */
@TestConfiguration
public class TestRestTemplateConfiguration {

    @LocalServerPort
    private int port;

    @Bean
    public TestRestTemplate testRestTemplate() {
        RestTemplateBuilder restTemplate = new RestTemplateBuilder();

        return new TestRestTemplate(restTemplate);
    }

}
