package com.example.jasperdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        // Create a RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        // Set the connection timeout and read timeout
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000); // 5 seconds
        requestFactory.setReadTimeout(5000); // 5 seconds

        // Set the request factory for the RestTemplate
        restTemplate.setRequestFactory(requestFactory);

        return restTemplate;
    }
}
