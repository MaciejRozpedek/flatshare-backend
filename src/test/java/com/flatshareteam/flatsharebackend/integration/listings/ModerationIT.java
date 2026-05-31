package com.flatshareteam.flatsharebackend.integration.listings;

import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.accounts.repository.LandlordRoleRepository;
import com.flatshareteam.flatsharebackend.accounts.repository.UserRepository;
import com.flatshareteam.flatsharebackend.integration.BaseIntegrationTest;
import com.flatshareteam.flatsharebackend.listings.model.*;
import com.flatshareteam.flatsharebackend.listings.repository.ApartmentRepository;
import com.flatshareteam.flatsharebackend.listings.repository.ListingRepository;
import com.flatshareteam.flatsharebackend.listings.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ModerationIT extends BaseIntegrationTest {

    @Autowired private UserRepository userRepository;
    @Autowired private LandlordRoleRepository landlordRoleRepository;
    @Autowired private ApartmentRepository apartmentRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private ListingRepository listingRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldHideListingByModeration() throws Exception {
        User landlord = ListingTestData.createAndSaveLandlord(userRepository, passwordEncoder, "landlord-moderation@test.com");
        Listing listing = ListingTestData.createAndSaveListing(
                apartmentRepository,
                roomRepository,
                listingRepository,
                landlordRoleRepository,
                landlord
        );

        mockMvc.perform(patch("/api/v1/listings/{listingId}/moderation/hide", listing.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listingId").value(listing.getId().toString()))
                .andExpect(jsonPath("$.status").value(ListingStatus.HIDDEN_BY_MODERATION.name()));

        Listing savedListing = listingRepository.findById(listing.getId()).orElseThrow();
        assertThat(savedListing.getStatus()).isEqualTo(ListingStatus.HIDDEN_BY_MODERATION);
    }
}
