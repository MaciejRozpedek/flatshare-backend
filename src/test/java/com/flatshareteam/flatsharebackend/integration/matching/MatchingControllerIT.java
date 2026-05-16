package com.flatshareteam.flatsharebackend.integration.matching;

import com.flatshareteam.flatsharebackend.accounts.model.AccountStatus;
import com.flatshareteam.flatsharebackend.accounts.model.LandlordRole;
import com.flatshareteam.flatsharebackend.accounts.model.TenantRole;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.accounts.repository.LandlordRoleRepository;
import com.flatshareteam.flatsharebackend.accounts.repository.UserRepository;
import com.flatshareteam.flatsharebackend.listings.model.*;
import com.flatshareteam.flatsharebackend.listings.repository.ApartmentRepository;
import com.flatshareteam.flatsharebackend.listings.repository.ListingRepository;
import com.flatshareteam.flatsharebackend.listings.repository.RoomRepository;
import com.flatshareteam.flatsharebackend.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MatchingControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private LandlordRoleRepository landlordRoleRepository;
    @Autowired private ApartmentRepository apartmentRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private ListingRepository listingRepository;
    @Autowired private JwtService jwtService;

    private String tenantToken;
    private String landlordToken;
    private LandlordRole landlordRole;

    @BeforeEach
    void setUp() {
        User landlord = createLandlord();
        landlordRole = landlordRoleRepository.findByUserId(landlord.getId()).orElseThrow();
        landlordToken = "Bearer " + jwtService.generateToken(landlord);
        tenantToken = "Bearer " + jwtService.generateToken(createTenantWithPreferences());
    }

    // Auth
    @Test
    void getMatches_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/matches"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMatches_ShouldReturn403_WhenUserIsNotTenant() throws Exception {
        mockMvc.perform(get("/api/v1/matches")
                        .header(HttpHeaders.AUTHORIZATION, landlordToken))
                .andExpect(status().isForbidden());
    }

    // Basic
    @Test
    void getMatches_ShouldReturn200_WhenTenantHasPreferences() throws Exception {
        mockMvc.perform(get("/api/v1/matches")
                        .header(HttpHeaders.AUTHORIZATION, tenantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getMatches_ShouldReturn200WithEmptyList_WhenTenantHasNoPreferencesSet() throws Exception {
        User tenantWithoutPrefs = new User();
        tenantWithoutPrefs.setFirstName("No");
        tenantWithoutPrefs.setLastName("Prefs");
        tenantWithoutPrefs.setEmail("noprefs@test.com");
        tenantWithoutPrefs.setPasswordHash("hash");
        tenantWithoutPrefs.setStatus(AccountStatus.ACTIVE);

        TenantRole role = new TenantRole();
        tenantWithoutPrefs.addRole(role);
        userRepository.save(tenantWithoutPrefs);

        String token = "Bearer " + jwtService.generateToken(tenantWithoutPrefs);

        mockMvc.perform(get("/api/v1/matches")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getMatches_ShouldReturnListing_WhenActiveListingExists() throws Exception {
        createListing("Warszawa", "Mokotów", new BigDecimal("1500"), true, ListingStatus.ACTIVE);

        mockMvc.perform(get("/api/v1/matches")
                        .header(HttpHeaders.AUTHORIZATION, tenantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getMatches_ShouldNotReturnListing_WhenListingIsNotActive() throws Exception {
        createListing("Warszawa", "Mokotów", new BigDecimal("1500"), true, ListingStatus.DRAFT);
        createListing("Warszawa", "Mokotów", new BigDecimal("1500"), true, ListingStatus.HIDDEN);

        mockMvc.perform(get("/api/v1/matches")
                        .header(HttpHeaders.AUTHORIZATION, tenantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // Filtering
    @Test
    void getMatches_ShouldFilterByCity() throws Exception {
        createListing("Warszawa", "Mokotów", new BigDecimal("1500"), true, ListingStatus.ACTIVE);
        createListing("Kraków", "Krowodrza", new BigDecimal("1500"), true, ListingStatus.ACTIVE);

        mockMvc.perform(get("/api/v1/matches?city=Warszawa")
                        .header(HttpHeaders.AUTHORIZATION, tenantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].listing.location.city").value("Warszawa"));
    }

    @Test
    void getMatches_ShouldFilterByMaxPrice() throws Exception {
        createListing("Warszawa", "Mokotów", new BigDecimal("1000"), true, ListingStatus.ACTIVE);
        createListing("Warszawa", "Mokotów", new BigDecimal("3000"), true, ListingStatus.ACTIVE);

        mockMvc.perform(get("/api/v1/matches?maxPrice=2000")
                        .header(HttpHeaders.AUTHORIZATION, tenantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getMatches_ShouldFilterByPetsAllowed() throws Exception {
        createListing("Warszawa", "Mokotów", new BigDecimal("1500"), true, ListingStatus.ACTIVE);
        createListing("Warszawa", "Mokotów", new BigDecimal("1500"), false, ListingStatus.ACTIVE);

        mockMvc.perform(get("/api/v1/matches?petsAllowed=true")
                        .header(HttpHeaders.AUTHORIZATION, tenantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getMatches_ShouldReturnEmptyList_WhenNoCityMatches() throws Exception {
        createListing("Kraków", "Krowodrza", new BigDecimal("1500"), true, ListingStatus.ACTIVE);

        mockMvc.perform(get("/api/v1/matches?city=Gdańsk")
                        .header(HttpHeaders.AUTHORIZATION, tenantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // Pagination
    @Test
    void getMatches_ShouldRespectPageSize() throws Exception {
        createListing("Warszawa", "Mokotów", new BigDecimal("1000"), true, ListingStatus.ACTIVE);
        createListing("Warszawa", "Mokotów", new BigDecimal("1100"), true, ListingStatus.ACTIVE);
        createListing("Warszawa", "Mokotów", new BigDecimal("1200"), true, ListingStatus.ACTIVE);

        mockMvc.perform(get("/api/v1/matches?size=2")
                        .header(HttpHeaders.AUTHORIZATION, tenantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    private User createTenantWithPreferences() {
        User user = new User();
        user.setFirstName("Jan");
        user.setLastName("Lokator");
        user.setEmail("tenant@test.com");
        user.setPasswordHash("hash");
        user.setStatus(AccountStatus.ACTIVE);

        TenantRole role = new TenantRole();
        role.setMaxPrice(new BigDecimal("2000"));
        role.setCurrency("PLN");
        role.setPetsAllowed(true);
        role.setSmokingAllowed(false);

        user.addRole(role);
        return userRepository.save(user);
    }

    private User createLandlord() {
        User user = new User();
        user.setFirstName("Joe");
        user.setLastName("Charles");
        user.setEmail("landlord@test.com");
        user.setPasswordHash("hash");
        user.setStatus(AccountStatus.ACTIVE);

        LandlordRole role =
                new LandlordRole();
        user.addRole(role);
        return userRepository.save(user);
    }

    private void createListing(String city, String district, BigDecimal price, boolean petsAllowed, ListingStatus status) {
        Apartment apartment = apartmentRepository.save(Apartment.builder()
                .city(city)
                .district(district)
                .street("Testowa")
                .aptNumber("1")
                .build());

        Room room = roomRepository.save(Room.builder()
                .area(20f)
                .apartment(apartment)
                .pricePerMonth(new Money(price, "PLN"))
                .build());

        listingRepository.save(Listing.builder()
                .title("Test listing")
                .price(new Money(price, "PLN"))
                .status(status)
                .createdAt(Instant.now())
                .availableSince(LocalDate.now())
                .availableUntil(LocalDate.now().plusYears(1))
                .ownerContact("123456789")
                .attributes(ListingAttributes.builder().petsAllowed(petsAllowed).build())
                .room(room)
                .landlordRole(landlordRole)
                .build());
    }
}