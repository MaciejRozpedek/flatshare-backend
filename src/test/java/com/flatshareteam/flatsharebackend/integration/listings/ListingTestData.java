package com.flatshareteam.flatsharebackend.integration.listings;

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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

final class ListingTestData {

    static final String DEFAULT_PASSWORD = "TestPassword123!";

    private ListingTestData() {
    }

    static User createAndSaveLandlord(UserRepository userRepository, PasswordEncoder passwordEncoder, String email) {
        User user = new User();
        user.setFirstName("Landlord");
        user.setLastName("Test");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setStatus(AccountStatus.ACTIVE);

        LandlordRole role = new LandlordRole();
        user.addRole(role);

        return userRepository.save(user);
    }

    static User createAndSaveTenant(UserRepository userRepository, PasswordEncoder passwordEncoder, String email) {
        User user = new User();
        user.setFirstName("Tenant");
        user.setLastName("Test");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setStatus(AccountStatus.ACTIVE);

        TenantRole role = new TenantRole();
        user.addRole(role);

        return userRepository.save(user);
    }

    static Listing createAndSaveListing(
            ApartmentRepository apartmentRepository,
            RoomRepository roomRepository,
            ListingRepository listingRepository,
            LandlordRoleRepository landlordRoleRepository,
            User landlord
    ) {
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
}
