package com.example.jasperdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class JasperMVP {
    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(JasperMVP.class);
        builder.headless(false);

        ConfigurableApplicationContext context = builder.run(args);
    }
}
