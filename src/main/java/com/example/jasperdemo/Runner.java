package com.example.jasperdemo;

import com.example.jasperdemo.config.ApplicationProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class Runner {
    public static void main(String[] args) {
        SpringApplicationBuilder app = new SpringApplicationBuilder(Runner.class);
        app.run(args);
    }

}
