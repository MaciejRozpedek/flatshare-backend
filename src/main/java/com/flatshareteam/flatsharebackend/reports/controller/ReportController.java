package com.flatshareteam.flatsharebackend.reports.controller;

import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.reports.dto.CreateReportRequest;
import com.flatshareteam.flatsharebackend.reports.dto.ReportResponse;
import com.flatshareteam.flatsharebackend.reports.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * POST /api/v1/reports
     * Accessible to any authenticated user (TENANT or LANDLORD).
     * Creates a new violation report with status OPEN.
     */
    @PostMapping("/api/v1/reports")
    public ResponseEntity<ReportResponse> createReport(
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal User authenticatedUser) {

        ReportResponse response = reportService.createReport(request, authenticatedUser.getId());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    /**
     * GET /api/v1/admin/reports
     * Restricted to ADMIN role only.
     * Returns a paginated list of all violation reports for the admin panel.
     */
    @GetMapping("/api/v1/admin/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReportResponse>> getAllReports(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(reportService.getAllReports(pageable));
    }
}
