package com.example.jasperdemo.service;

public record DataSourceDto(String label,
                            String driverClass,
                            String username,
                            String password,
                            String connectionUrl) {
}
