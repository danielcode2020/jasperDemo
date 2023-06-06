package com.example.jasperdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jasperdemo", ignoreUnknownFields = false)
public record ApplicationProperties(String jasperServerUrl, String username, String password) { }
