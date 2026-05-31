package com.flatshareteam.flatsharebackend.reports.dto;

import com.flatshareteam.flatsharebackend.reports.model.ViolationReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateReportRequest(
        @NotNull(message = "type is required")
        ViolationReportType type,

        @NotNull(message = "targetId is required")
        UUID targetId,

        @NotBlank(message = "reason is required")
        String reason,

        String details
) {}
