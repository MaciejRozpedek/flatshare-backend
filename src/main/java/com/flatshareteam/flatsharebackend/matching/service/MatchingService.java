package com.flatshareteam.flatsharebackend.matching.service;

import com.flatshareteam.flatsharebackend.accounts.model.TenantRole;
import com.flatshareteam.flatsharebackend.accounts.repository.TenantRoleRepository;
import com.flatshareteam.flatsharebackend.listings.mapper.ListingMapper;
import com.flatshareteam.flatsharebackend.listings.model.Listing;
import com.flatshareteam.flatsharebackend.listings.model.ListingAttributes;
import com.flatshareteam.flatsharebackend.listings.model.ListingStatus;
import com.flatshareteam.flatsharebackend.listings.model.Room;
import com.flatshareteam.flatsharebackend.listings.repository.ListingRepository;
import com.flatshareteam.flatsharebackend.matching.dto.MatchFilterCriteria;
import com.flatshareteam.flatsharebackend.matching.dto.MatchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final ListingRepository listingRepository;
    private final TenantRoleRepository tenantRoleRepository;
    private final ListingMapper listingMapper;

    public Page<MatchResult> getMatches(UUID userId, MatchFilterCriteria filter, Pageable pageable) {
        TenantRole preferences = tenantRoleRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant preferences not found"));

        List<MatchResult> results = listingRepository.findAll().stream()
                .filter(l -> l.getStatus() == ListingStatus.ACTIVE)
                .filter(l -> matchesFilter(l, filter))
                .map(l -> new MatchResult(listingMapper.toDto(l), computeScore(l, preferences)))
                .sorted(Comparator.comparingDouble(MatchResult::matchScore).reversed())
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), results.size());

        if (start >= results.size()) {
            return new PageImpl<>(List.of(), pageable, results.size());
        }

        return new PageImpl<>(results.subList(start, end), pageable, results.size());
    }

    private boolean matchesFilter(Listing listing, MatchFilterCriteria f) {
        if (f == null) return true;

        Room room = listing.getRoom();
        var apartment = room != null ? room.getApartment() : null;
        ListingAttributes attr = listing.getAttributes();

        if (f.city() != null && (apartment == null || !f.city().equalsIgnoreCase(apartment.getCity())))
            return false;
        if (f.district() != null && (apartment == null || !f.district().equalsIgnoreCase(apartment.getDistrict())))
            return false;
        if (f.minPrice() != null && (listing.getPrice() == null || listing.getPrice().getAmount().compareTo(f.minPrice()) < 0))
            return false;
        if (f.maxPrice() != null && (listing.getPrice() == null || listing.getPrice().getAmount().compareTo(f.maxPrice()) > 0))
            return false;
        if (f.petsAllowed() != null && (attr == null || attr.isPetsAllowed() != f.petsAllowed()))
            return false;
        if (f.nonSmokingOnly() != null && (attr == null || attr.isNonSmokingOnly() != f.nonSmokingOnly()))
            return false;
        if (f.closeToShops() != null && (attr == null || attr.isCloseToShops() != f.closeToShops()))
            return false;
        if (f.profile() != null && (attr == null || !f.profile().equalsIgnoreCase(attr.getProfile())))
            return false;
        if (f.minArea() != null && (room == null || room.getArea() == null || room.getArea() < f.minArea()))
            return false;
        if (f.maxArea() != null && (room == null || room.getArea() == null || room.getArea() > f.maxArea()))
            return false;
        if (f.startDate() != null && (listing.getAvailableSince() == null || listing.getAvailableSince().isAfter(f.startDate())))
            return false;

        return true;
    }

    private double computeScore(Listing listing, TenantRole prefs) {
        int total = 0;
        int matched = 0;

        if (prefs.getMaxPrice() != null && listing.getPrice() != null) {
            total++;
            if (listing.getPrice().getAmount().compareTo(prefs.getMaxPrice()) <= 0) matched++;
        }

        if (prefs.getPetsAllowed() != null && listing.getAttributes() != null) {
            total++;
            if (!prefs.getPetsAllowed() || listing.getAttributes().isPetsAllowed()) matched++;
        }

        if (prefs.getSmokingAllowed() != null && listing.getAttributes() != null) {
            total++;
            if (prefs.getSmokingAllowed() || listing.getAttributes().isNonSmokingOnly()) matched++;
        }

        if (prefs.getPreferredDistricts() != null && !prefs.getPreferredDistricts().isEmpty()
                && listing.getRoom() != null && listing.getRoom().getApartment() != null) {
            total++;
            if (prefs.getPreferredDistricts().contains(listing.getRoom().getApartment().getDistrict())) matched++;
        }

        return total == 0 ? 1.0 : (double) matched / total;
    }
}
