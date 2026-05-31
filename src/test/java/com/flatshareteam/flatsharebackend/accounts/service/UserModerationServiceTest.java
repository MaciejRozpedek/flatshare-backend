package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.dto.BanUserResponse;
import com.flatshareteam.flatsharebackend.accounts.model.AccountStatus;
import com.flatshareteam.flatsharebackend.accounts.model.LandlordRole;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserModerationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private ViolationReportRepository violationReportRepository;

    @InjectMocks
    private UserModerationService userModerationService;

    @Test
    void banUserBlocksAccountHidesListingsCancelsBookingsInitiatesRefundsAndRegistersReport() {
        UUID userId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        String reason = "Repeated rule violations";

        User user = new User();
        user.setId(userId);
        user.setStatus(AccountStatus.ACTIVE);

        Listing listing = buildListing(userId);
        Booking booking = buildBooking(listing);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(listingRepository.findByLandlordRoleUserIdAndStatus(userId, ListingStatus.ACTIVE))
                .thenReturn(List.of(listing));
        when(bookingRepository.findByListingLandlordRoleUserIdAndStatusAndStartDateAfter(
                userId,
                BookingStatus.CONFIRMED,
                LocalDate.now()
        )).thenReturn(List.of(booking));
        when(paymentService.initiateRefundForBooking(booking.getId(), reason))
                .thenReturn(new PaymentService.RefundInitiationResult(paymentId, "refund-1"));
        when(violationReportRepository.save(any(ViolationReport.class))).thenAnswer(invocation -> {
            ViolationReport report = invocation.getArgument(0);
            report.setId(reportId);
            return report;
        });

        BanUserResponse response = userModerationService.banUser(userId, moderatorId, reason);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.status()).isEqualTo(AccountStatus.BLOCKED);
        assertThat(response.moderationReportId()).isEqualTo(reportId);
        assertThat(response.hiddenListingsCount()).isEqualTo(1);
        assertThat(response.cancelledBookingsCount()).isEqualTo(1);
        assertThat(response.initiatedRefundsCount()).isEqualTo(1);

        assertThat(user.getStatus()).isEqualTo(AccountStatus.BLOCKED);
        assertThat(listing.getStatus()).isEqualTo(ListingStatus.HIDDEN_BY_MODERATION);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getDecisionAt()).isNotNull();
        assertThat(booking.getDecisionReason()).isEqualTo(reason);

        verify(userRepository).save(user);
        verify(listingRepository).saveAll(List.of(listing));
        verify(bookingRepository).saveAll(List.of(booking));
        verify(paymentService).initiateRefundForBooking(booking.getId(), reason);

        ArgumentCaptor<ViolationReport> reportCaptor = ArgumentCaptor.forClass(ViolationReport.class);
        verify(violationReportRepository).save(reportCaptor.capture());
        ViolationReport report = reportCaptor.getValue();
        assertThat(report.getReporterId()).isEqualTo(moderatorId);
        assertThat(report.getType()).isEqualTo(ViolationReportType.USER);
        assertThat(report.getTargetId()).isEqualTo(userId);
        assertThat(report.getReason()).isEqualTo(reason);
        assertThat(report.getStatus()).isEqualTo(ViolationReportStatus.ACTION_TAKEN);
        assertThat(report.getDetails()).contains(
                "hiddenListings=1",
                listing.getId().toString(),
                "cancelledBookings=1",
                booking.getId().toString(),
                "initiatedRefunds=1",
                paymentId.toString(),
                "refund-1"
        );
    }

    private Listing buildListing(UUID ownerId) {
        User owner = new User();
        owner.setId(ownerId);
        LandlordRole landlordRole = new LandlordRole();
        landlordRole.setUser(owner);

        Listing listing = new Listing();
        listing.setId(UUID.randomUUID());
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setLandlordRole(landlordRole);
        return listing;
    }

    private Booking buildBooking(Listing listing) {
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setListing(listing);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setStartDate(LocalDate.now().plusDays(1));
        booking.setEndDate(LocalDate.now().plusDays(30));
        return booking;
    }
}
