package com.flatshareteam.flatsharebackend.matching.service;

import com.flatshareteam.flatsharebackend.accounts.model.TenantRole;
import com.flatshareteam.flatsharebackend.accounts.repository.TenantRoleRepository;
import com.flatshareteam.flatsharebackend.listings.mapper.ListingMapper;
import com.flatshareteam.flatsharebackend.listings.model.Listing;
import com.flatshareteam.flatsharebackend.listings.repository.ListingRepository;
import com.flatshareteam.flatsharebackend.matching.specification.MatchingSpecifications;
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

        List<MatchResult> results = listingRepository.findAll(MatchingSpecifications.fromFilter(filter)).stream()
                .map(l -> new MatchResult(listingMapper.toDto(l), computeScore(l, preferences)))
                .sorted(Comparator.comparingDouble(MatchResult::matchScore).reversed()
                        .thenComparing(r -> r.listing().id()))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), results.size());

        if (start >= results.size()) {
            return new PageImpl<>(List.of(), pageable, results.size());
        }

        return new PageImpl<>(results.subList(start, end), pageable, results.size());
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