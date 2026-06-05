package com.flatshareteam.flatsharebackend.reports.service;

import com.flatshareteam.flatsharebackend.reports.dto.CreateReportRequest;
import com.flatshareteam.flatsharebackend.reports.dto.ReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReportService {

    /**
     * Creates a new violation report with status OPEN.
     *
     * @param request    the report payload (type, targetId, reason, details)
     * @param reporterId the UUID of the authenticated user submitting the report
     * @return the created report as a response DTO
     */
    ReportResponse createReport(CreateReportRequest request, UUID reporterId);

    /**
     * Returns a paginated list of all violation reports (admin-only use case).
     *
     * @param pageable pagination and sorting parameters
     * @return a page of report response DTOs
     */
    Page<ReportResponse> getAllReports(Pageable pageable);
}
