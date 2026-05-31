package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.dto.BanUserResponse;
import com.flatshareteam.flatsharebackend.accounts.model.AccountStatus;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.accounts.repository.UserRepository;
import com.flatshareteam.flatsharebackend.bookings.model.Booking;
import com.flatshareteam.flatsharebackend.bookings.model.BookingStatus;
import com.flatshareteam.flatsharebackend.bookings.repository.BookingRepository;
import com.flatshareteam.flatsharebackend.listings.model.Listing;
import com.flatshareteam.flatsharebackend.listings.model.ListingStatus;
import com.flatshareteam.flatsharebackend.listings.repository.ListingRepository;
import com.flatshareteam.flatsharebackend.payments.service.PaymentService;
import com.flatshareteam.flatsharebackend.reports.model.ViolationReport;
import com.flatshareteam.flatsharebackend.reports.model.ViolationReportStatus;
import com.flatshareteam.flatsharebackend.reports.model.ViolationReportType;
import com.flatshareteam.flatsharebackend.reports.repository.ViolationReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserModerationService {

    private static final String DEFAULT_BAN_REASON = "User blocked by moderation";

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final BookingRepository bookingRepository;
    private final PaymentService paymentService;
    private final ViolationReportRepository violationReportRepository;

    @Transactional
    public BanUserResponse banUser(UUID userId, UUID moderatorId, String reason) {
        String normalizedReason = normalizeReason(reason);
        Instant actionAt = Instant.now();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setStatus(AccountStatus.BLOCKED);
        userRepository.save(user);

        List<Listing> hiddenListings = hideActiveListings(userId);
        List<Booking> cancelledBookings = cancelFutureConfirmedBookings(userId, normalizedReason, actionAt);
        List<RefundAuditEntry> refunds = initiateRefunds(cancelledBookings, normalizedReason);

        ViolationReport moderationReport = registerModerationReport(
                userId,
                moderatorId,
                normalizedReason,
                hiddenListings,
                cancelledBookings,
                refunds,
                actionAt
        );

        return new BanUserResponse(
                user.getId(),
                user.getStatus(),
                moderationReport.getId(),
                hiddenListings.size(),
                cancelledBookings.size(),
                refunds.size()
        );
    }

    private List<Listing> hideActiveListings(UUID userId) {
        List<Listing> activeListings = listingRepository.findByLandlordRoleUserIdAndStatus(
                userId,
                ListingStatus.ACTIVE
        );

        activeListings.forEach(listing -> listing.setStatus(ListingStatus.HIDDEN_BY_MODERATION));
        listingRepository.saveAll(activeListings);
        return activeListings;
    }

    private List<Booking> cancelFutureConfirmedBookings(UUID userId, String reason, Instant cancelledAt) {
        List<Booking> futureConfirmedBookings =
                bookingRepository.findByListingLandlordRoleUserIdAndStatusAndStartDateAfter(
                        userId,
                        BookingStatus.CONFIRMED,
                        LocalDate.now()
                );

        futureConfirmedBookings.forEach(booking -> {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setDecisionAt(cancelledAt);
            booking.setDecisionReason(reason);
        });
        bookingRepository.saveAll(futureConfirmedBookings);
        return futureConfirmedBookings;
    }

    private List<RefundAuditEntry> initiateRefunds(List<Booking> bookings, String reason) {
        List<RefundAuditEntry> refunds = new java.util.ArrayList<>();
        for (Booking booking : bookings) {
            PaymentService.RefundInitiationResult result = paymentService.initiateRefundForBooking(
                    booking.getId(),
                    reason
            );
            refunds.add(new RefundAuditEntry(
                    booking.getId(),
                    result.paymentId(),
                    result.refundReference()
            ));
        }
        return refunds;
    }

    private ViolationReport registerModerationReport(
            UUID userId,
            UUID moderatorId,
            String reason,
            List<Listing> hiddenListings,
            List<Booking> cancelledBookings,
            List<RefundAuditEntry> refunds,
            Instant actionAt
    ) {
        ViolationReport report = ViolationReport.builder()
                .reporterId(moderatorId != null ? moderatorId : userId)
                .type(ViolationReportType.USER)
                .targetId(userId)
                .reason(reason)
                .details("USER_BANNED at " + actionAt
                        + "; hiddenListings=" + hiddenListings.size()
                        + "; hiddenListingIds=" + listingIds(hiddenListings)
                        + "; cancelledBookings=" + cancelledBookings.size()
                        + "; cancelledBookingIds=" + bookingIds(cancelledBookings)
                        + "; initiatedRefunds=" + refunds.size()
                        + "; refunds=" + refunds)
                .status(ViolationReportStatus.ACTION_TAKEN)
                .createdAt(actionAt)
                .build();
        return violationReportRepository.save(report);
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return DEFAULT_BAN_REASON;
        }
        return reason.trim();
    }

    private List<UUID> listingIds(List<Listing> listings) {
        return listings.stream()
                .map(Listing::getId)
                .toList();
    }

    private List<UUID> bookingIds(List<Booking> bookings) {
        return bookings.stream()
                .map(Booking::getId)
                .toList();
    }

    private record RefundAuditEntry(UUID bookingId, UUID paymentId, String refundReference) {
    }
}
