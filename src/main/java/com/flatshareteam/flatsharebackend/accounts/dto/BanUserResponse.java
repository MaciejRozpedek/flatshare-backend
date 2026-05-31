package com.flatshareteam.flatsharebackend.accounts.dto;

import com.flatshareteam.flatsharebackend.accounts.model.AccountStatus;

import java.util.UUID;

public record BanUserResponse(
        UUID userId,
        AccountStatus status,
        UUID moderationReportId,
        int hiddenListingsCount,
        int cancelledBookingsCount,
        int initiatedRefundsCount
) {
}
