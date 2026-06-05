package com.flatshareteam.flatsharebackend.reports.repository;

import com.flatshareteam.flatsharebackend.reports.model.ViolationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ViolationReportRepository extends JpaRepository<ViolationReport, UUID> {
}
