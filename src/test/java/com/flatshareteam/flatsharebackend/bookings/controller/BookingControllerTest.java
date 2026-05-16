package com.flatshareteam.flatsharebackend.bookings.controller;

import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.bookings.dto.BookingCreateRequest;
import com.flatshareteam.flatsharebackend.bookings.dto.BookingCreateResponse;
import com.flatshareteam.flatsharebackend.bookings.dto.BookingDecisionRequest;
import com.flatshareteam.flatsharebackend.bookings.dto.BookingStatusResponse;
import com.flatshareteam.flatsharebackend.bookings.model.BookingStatus;
import com.flatshareteam.flatsharebackend.bookings.service.BookingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    @Test
    void shouldCreateBooking() {
        // given
        UUID roomId = UUID.randomUUID();
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusMonths(3);
        BookingCreateRequest request = new BookingCreateRequest(roomId, start, end);
        User user = new User();
        user.setId(UUID.randomUUID());

        UUID rentalId = UUID.randomUUID();
        BookingCreateResponse mockResponse = new BookingCreateResponse(
                rentalId,
                BookingStatus.PENDING_APPROVAL,
                Instant.now(),
                BigDecimal.valueOf(100),
                "PLN",
                "/api/v1/bookings/" + rentalId
        );

        when(bookingService.create(request, user.getId())).thenReturn(mockResponse);

        // when
        ResponseEntity<BookingCreateResponse> response = bookingController.create(request, user);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation().toString()).isEqualTo("/api/v1/bookings/" + rentalId);
        assertThat(response.getBody()).isEqualTo(mockResponse);
    }

    @Test
    void shouldCancelBookingWithReason() {
        // given
        UUID bookingId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        BookingDecisionRequest request = new BookingDecisionRequest("Changed mind");

        BookingStatusResponse mockResponse = new BookingStatusResponse(
                bookingId,
                BookingStatus.CANCELLED,
                null, null, Instant.now(), "Changed mind"
        );

        when(bookingService.cancel(bookingId, user.getId(), "Changed mind")).thenReturn(mockResponse);

        // when
        ResponseEntity<BookingStatusResponse> response = bookingController.cancel(bookingId, user, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockResponse);
    }

    @Test
    void shouldCancelBookingWithoutReason() {
        // given
        UUID bookingId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());

        BookingStatusResponse mockResponse = new BookingStatusResponse(
                bookingId,
                BookingStatus.CANCELLED,
                null, null, Instant.now(), null
        );

        when(bookingService.cancel(bookingId, user.getId(), null)).thenReturn(mockResponse);

        // when
        ResponseEntity<BookingStatusResponse> response = bookingController.cancel(bookingId, user, null);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockResponse);
    }

    @Test
    void shouldGetBookingStatusWithAuthenticatedUser() {
        // given
        UUID bookingId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());

        BookingStatusResponse mockResponse = new BookingStatusResponse(
                bookingId,
                BookingStatus.PENDING_PAYMENT,
                Instant.now(), Instant.now().plusSeconds(86400), null, null
        );

        when(bookingService.getStatus(bookingId, user.getId())).thenReturn(mockResponse);

        // when
        ResponseEntity<BookingStatusResponse> response = bookingController.getStatus(bookingId, user);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockResponse);
    }

    @Test
    void shouldGetBookingStatusWithUnauthenticatedIntegration() {
        // given
        UUID bookingId = UUID.randomUUID();

        BookingStatusResponse mockResponse = new BookingStatusResponse(
                bookingId,
                BookingStatus.PENDING_PAYMENT,
                Instant.now(), Instant.now().plusSeconds(86400), null, null
        );

        when(bookingService.getStatus(bookingId, null)).thenReturn(mockResponse);

        // when
        ResponseEntity<BookingStatusResponse> response = bookingController.getStatus(bookingId, null);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockResponse);
    }
}
