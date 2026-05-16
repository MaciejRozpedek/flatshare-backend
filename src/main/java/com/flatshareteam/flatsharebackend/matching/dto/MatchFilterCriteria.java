package com.flatshareteam.flatsharebackend.matching.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MatchFilterCriteria(
        String city,
        String district,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean petsAllowed,
        Boolean nonSmokingOnly,
        Boolean closeToShops,
        String profile,
        Double minArea,
        Double maxArea,
        LocalDate startDate
) {}
