package com.flatshareteam.flatsharebackend.integration.listings;

import com.flatshareteam.flatsharebackend.accounts.dto.LoginRequest;
import com.flatshareteam.flatsharebackend.accounts.dto.LoginResponse;
import com.flatshareteam.flatsharebackend.accounts.model.AccountStatus;
import com.flatshareteam.flatsharebackend.accounts.model.LandlordRole;
import com.flatshareteam.flatsharebackend.accounts.model.TenantRole;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.accounts.repository.LandlordRoleRepository;
import com.flatshareteam.flatsharebackend.accounts.repository.TenantRoleRepository;
import com.flatshareteam.flatsharebackend.accounts.repository.UserRepository;
import com.flatshareteam.flatsharebackend.bookings.model.Booking;
import com.flatshareteam.flatsharebackend.bookings.model.BookingStatus;
import com.flatshareteam.flatsharebackend.bookings.repository.BookingRepository;
import com.flatshareteam.flatsharebackend.integration.BaseIntegrationTest;
import com.flatshareteam.flatsharebackend.listings.model.*;
import com.flatshareteam.flatsharebackend.listings.repository.ApartmentRepository;
import com.flatshareteam.flatsharebackend.listings.repository.ListingRepository;
import com.flatshareteam.flatsharebackend.listings.repository.RoomRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UnavailabilityIT extends BaseIntegrationTest {

    @Autowired private UserRepository userRepository;
    @Autowired private LandlordRoleRepository landlordRoleRepository;
    @Autowired private TenantRoleRepository tenantRoleRepository;
    @Autowired private ApartmentRepository apartmentRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private ListingRepository listingRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EntityManager entityManager;

    private static final String PASSWORD = "TestPassword123!";
    private User landlord;
    private String landlordToken;
    private Listing testListing;

    @BeforeEach
    void setUp() throws Exception {
        landlord = createAndSaveLandlord("landlord@test.com");
        landlordToken = loginAndGetToken(landlord.getEmail());

        testListing = createAndSaveListing(landlord);
    }

    @Test
    void shouldSuccessfullyAddUnavailability() throws Exception {
        // given
        String requestBody = """
            {
                "Since": "2030-05-01",
                "Until": "2030-05-10",
                "Message": "Room painting"
            }
            """;

        // when & then
        mockMvc.perform(post("/api/v1/listings/{listingId}/unavailability", testListing.getId())
                        .header(HttpHeaders.AUTHORIZATION, landlordToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        Listing savedListing = listingRepository.findById(testListing.getId()).orElseThrow();
        assertThat(savedListing.getUnavailabilities()).hasSize(1);
        Unavailability saved = savedListing.getUnavailabilities().getFirst();
        assertThat(saved.getStartDate()).isEqualTo(LocalDate.parse("2030-05-01"));
        assertThat(saved.getReason()).isEqualTo("Room painting");
    }

    @Test
    void shouldReturn400WhenSinceIsAfterUntil() throws Exception {
        // given
        String requestBody = """
            {
                "Since": "2030-05-10",
                "Until": "2030-05-01",
                "Message": "Invalid dates"
            }
            """;

        // when & then
        mockMvc.perform(post("/api/v1/listings/{listingId}/unavailability", testListing.getId())
                        .header(HttpHeaders.AUTHORIZATION, landlordToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        Listing savedListing = listingRepository.findById(testListing.getId()).orElseThrow();
        assertThat(savedListing.getUnavailabilities()).isEmpty();
    }

    @Test
    void shouldReturn404WhenTryingToAddUnavailabilityToSomeoneElsesListing() throws Exception {
        // given
        User otherLandlord = createAndSaveLandlord("other@test.com");
        String otherLandlordToken = loginAndGetToken(otherLandlord.getEmail());

        String requestBody = """
            {
                "Since": "2030-05-01",
                "Until": "2030-05-10",
                "Message": "Unauthorized attempt"
            }
            """;

        // when & then
        mockMvc.perform(post("/api/v1/listings/{listingId}/unavailability", testListing.getId())
                        .header(HttpHeaders.AUTHORIZATION, otherLandlordToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn409WhenUnavailabilityOverlapsWithExistingBooking() throws Exception {
        // given
        User tenant = createAndSaveTenant("tenant@test.com");
        createBooking(testListing, tenant, LocalDate.parse("2030-06-01"), LocalDate.parse("2030-06-30"), BookingStatus.ACCEPTED);

        String requestBody = """
            {
                "Since": "2030-06-15",
                "Until": "2030-07-05",
                "Message": "Renovation"
            }
            """;

        // when & then
        mockMvc.perform(post("/api/v1/listings/{listingId}/unavailability", testListing.getId())
                        .header(HttpHeaders.AUTHORIZATION, landlordToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldSuccessfullyDeleteUnavailability() throws Exception {
        // given
        testListing.addUnavailability(Unavailability.builder()
                .startDate(LocalDate.parse("2030-08-01"))
                .endDate(LocalDate.parse("2030-08-10"))
                .reason("Test")
                .build());
        testListing = listingRepository.save(testListing);
        UUID unavailabilityId = testListing.getUnavailabilities().getFirst().getId();

        // when
        mockMvc.perform(delete("/api/v1/listings/{listingId}/unavailability/{unavailabilityId}",
                        testListing.getId(), unavailabilityId)
                        .header(HttpHeaders.AUTHORIZATION, landlordToken))
                .andExpect(status().isNoContent());

        // then
        entityManager.flush();
        entityManager.clear();
        Listing savedListing = listingRepository.findById(testListing.getId()).orElseThrow();
        assertThat(savedListing.getUnavailabilities()).isEmpty();
    }

    @Test
    void shouldReturnUnavailabilityInPascalCaseWhenGettingListing() throws Exception {
        // given
        testListing.addUnavailability(Unavailability.builder()
                .startDate(LocalDate.parse("2030-09-01"))
                .endDate(LocalDate.parse("2030-09-15"))
                .reason("PascalCase Check")
                .build());
        listingRepository.save(testListing);

        // when & then
        mockMvc.perform(get("/api/v1/listings/{listingId}", testListing.getId())
                        .header(HttpHeaders.AUTHORIZATION, landlordToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unavailability").isArray())
                .andExpect(jsonPath("$.unavailability[0].Since").value("2030-09-01"))
                .andExpect(jsonPath("$.unavailability[0].Until").value("2030-09-15"))
                .andExpect(jsonPath("$.unavailability[0].Message").value("PascalCase Check"));
    }


    private User createAndSaveLandlord(String email) {
        User user = new User();
        user.setFirstName("Landlord");
        user.setLastName("Test");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setStatus(AccountStatus.ACTIVE);

        LandlordRole role = new LandlordRole();
        user.addRole(role);

        return userRepository.save(user);
    }

    private User createAndSaveTenant(String email) {
        User user = new User();
        user.setFirstName("Tenant");
        user.setLastName("Test");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setStatus(AccountStatus.ACTIVE);

        TenantRole role = new TenantRole();
        user.addRole(role);

        return userRepository.save(user);
    }

    private Listing createAndSaveListing(User landlord) {
        Apartment apartment = apartmentRepository.save(Apartment.builder()
                .city("Warsaw")
                .district("Downtown")
                .street("Main Street")
                .aptNumber("10")
                .build());

        Room room = roomRepository.save(Room.builder()
                .area(15.5f)
                .pricePerMonth(new Money(BigDecimal.valueOf(2000), "PLN"))
                .apartment(apartment)
                .build());

        LandlordRole landlordRole = landlordRoleRepository.findByUserId(landlord.getId()).orElseThrow();

        return listingRepository.save(Listing.builder()
                .title("Cozy Room")
                .description("Very nice room")
                .price(new Money(BigDecimal.valueOf(2000), "PLN"))
                .status(ListingStatus.ACTIVE)
                .createdAt(Instant.now())
                .availableSince(LocalDate.now())
                .availableUntil(LocalDate.now().plusYears(1))
                .ownerContact("123456789")
                .attributes(ListingAttributes.builder().petsAllowed(false).nonSmokingOnly(true).build())
                .room(room)
                .landlordRole(landlordRole)
                .build());
    }

    private void createBooking(Listing listing, User tenant, LocalDate start, LocalDate end, BookingStatus status) {
        TenantRole tenantRole = tenantRoleRepository.findByUserId(tenant.getId()).orElseThrow();
        Booking booking = Booking.builder()
                .listing(listing)
                .tenantRole(tenantRole)
                .startDate(start)
                .endDate(end)
                .status(status)
                .createdAt(Instant.now())
                .build();
        bookingRepository.save(booking);
    }

    private String loginAndGetToken(String email) throws Exception {
        LoginRequest request = new LoginRequest(email, PASSWORD);

        String response = mockMvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponse loginResponse = jsonMapper.readValue(response, LoginResponse.class);
        return "Bearer " + loginResponse.token();
    }
}
