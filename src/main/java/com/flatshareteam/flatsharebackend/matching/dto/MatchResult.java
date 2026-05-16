package com.flatshareteam.flatsharebackend.matching.dto;

import com.flatshareteam.flatsharebackend.listings.dto.ListingDto;

public record MatchResult(
        ListingDto listing,
        double matchScore
) {}