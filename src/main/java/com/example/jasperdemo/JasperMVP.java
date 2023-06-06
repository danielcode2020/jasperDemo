package com.example.jasperdemo;

import com.example.jasperdemo.config.ApplicationProperties;
import com.example.jasperdemo.controller.JasperServerResource;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class JasperMVP {
    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(JasperMVP.class);
        builder.headless(false);

        ConfigurableApplicationContext context = builder.run(args);
    }
}
