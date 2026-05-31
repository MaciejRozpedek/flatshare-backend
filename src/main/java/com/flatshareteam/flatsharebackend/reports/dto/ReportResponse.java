package com.flatshareteam.flatsharebackend.reports.dto;

import com.flatshareteam.flatsharebackend.reports.model.ViolationReportStatus;
import com.flatshareteam.flatsharebackend.reports.model.ViolationReportType;

import java.time.Instant;
import java.util.UUID;

public record ReportResponse(
        UUID id,
        UUID reporterId,
        ViolationReportType type,
        UUID targetId,
        String reason,
        String details,
        ViolationReportStatus status,
        Instant createdAt
) {}
