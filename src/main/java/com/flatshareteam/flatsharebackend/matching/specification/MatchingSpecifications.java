package com.flatshareteam.flatsharebackend.matching.specification;

import com.flatshareteam.flatsharebackend.listings.model.Listing;
import com.flatshareteam.flatsharebackend.listings.model.ListingStatus;
import com.flatshareteam.flatsharebackend.matching.dto.MatchFilterCriteria;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MatchingSpecifications {

    private MatchingSpecifications() {}

    public static Specification<Listing> fromFilter(MatchFilterCriteria f) {
        Specification<Listing> spec = (root, query, cb)
                -> cb.equal(root.get("status"), ListingStatus.ACTIVE);

        if (f == null) return spec;

        if (f.city() != null)           spec = spec.and(cityEquals(f.city()));
        if (f.district() != null)       spec = spec.and(districtEquals(f.district()));
        if (f.minPrice() != null)       spec = spec.and(minPrice(f.minPrice()));
        if (f.maxPrice() != null)       spec = spec.and(maxPrice(f.maxPrice()));
        if (f.petsAllowed() != null)    spec = spec.and(petsAllowed(f.petsAllowed()));
        if (f.nonSmokingOnly() != null) spec = spec.and(nonSmokingOnly(f.nonSmokingOnly()));
        if (f.closeToShops() != null)   spec = spec.and(closeToShops(f.closeToShops()));
        if (f.profile() != null)        spec = spec.and(profileEquals(f.profile()));
        if (f.minArea() != null)        spec = spec.and(minArea(f.minArea()));
        if (f.maxArea() != null)        spec = spec.and(maxArea(f.maxArea()));
        if (f.startDate() != null)      spec = spec.and(availableSince(f.startDate()));

        return spec;
    }

    private static Specification<Listing> cityEquals(String city) {
        if (city == null) return null;
        return (root, query, cb) -> cb.equal(
                cb.lower(root.get("room").get("apartment").get("city")), city.toLowerCase());
    }

    private static Specification<Listing> districtEquals(String district) {
        if (district == null) return null;
        return (root, query, cb) -> cb.equal(
                cb.lower(root.get("room").get("apartment").get("district")), district.toLowerCase());
    }

    private static Specification<Listing> minPrice(BigDecimal min) {
        if (min == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price").get("amount"), min);
    }

    private static Specification<Listing> maxPrice(BigDecimal max) {
        if (max == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price").get("amount"), max);
    }

    private static Specification<Listing> petsAllowed(Boolean petsAllowed) {
        if (petsAllowed == null) return null;
        return (root, query, cb) -> cb.equal(root.get("attributes").get("petsAllowed"), petsAllowed);
    }

    private static Specification<Listing> nonSmokingOnly(Boolean nonSmokingOnly) {
        if (nonSmokingOnly == null) return null;
        return (root, query, cb) -> cb.equal(root.get("attributes").get("nonSmokingOnly"), nonSmokingOnly);
    }

    private static Specification<Listing> closeToShops(Boolean closeToShops) {
        if (closeToShops == null) return null;
        return (root, query, cb) -> cb.equal(root.get("attributes").get("closeToShops"), closeToShops);
    }

    private static Specification<Listing> profileEquals(String profile) {
        if (profile == null) return null;
        return (root, query, cb) -> cb.equal(
                cb.lower(root.get("attributes").get("profile")), profile.toLowerCase());
    }

    private static Specification<Listing> minArea(Double min) {
        if (min == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("room").get("area"), min);
    }

    private static Specification<Listing> maxArea(Double max) {
        if (max == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("room").get("area"), max);
    }

    private static Specification<Listing> availableSince(LocalDate startDate) {
        if (startDate == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("availableSince"), startDate);
    }
}