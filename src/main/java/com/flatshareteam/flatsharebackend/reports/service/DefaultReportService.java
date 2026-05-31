package com.flatshareteam.flatsharebackend.reports.service;

import com.flatshareteam.flatsharebackend.reports.dto.CreateReportRequest;
import com.flatshareteam.flatsharebackend.reports.dto.ReportResponse;
import com.flatshareteam.flatsharebackend.reports.model.ViolationReport;
import com.flatshareteam.flatsharebackend.reports.repository.ViolationReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefaultReportService implements ReportService {

    private final ViolationReportRepository reportRepository;

    @Override
    @Transactional
    public ReportResponse createReport(CreateReportRequest request, UUID reporterId) {
        ViolationReport report = ViolationReport.builder()
                .reporterId(reporterId)
                .type(request.type())
                .targetId(request.targetId())
                .reason(request.reason())
                .details(request.details())
                .build();

        ViolationReport saved = reportRepository.save(report);
        return toResponse(saved);
    }

    @Override
    public Page<ReportResponse> getAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable).map(this::toResponse);
    }

    private ReportResponse toResponse(ViolationReport report) {
        return new ReportResponse(
                report.getId(),
                report.getReporterId(),
                report.getType(),
                report.getTargetId(),
                report.getReason(),
                report.getDetails(),
                report.getStatus(),
                report.getCreatedAt()
        );
    }
}
