package com.example.jasperdemo.service;

import jakarta.validation.constraints.NotNull;

public record ReportDto (@NotNull String label,
                         @NotNull String type,
                         @NotNull String data,
                         @NotNull Long dataSourceId) {}
