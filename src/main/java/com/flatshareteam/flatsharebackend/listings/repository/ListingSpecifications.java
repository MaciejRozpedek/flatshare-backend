package com.flatshareteam.flatsharebackend.listings.repository;

import com.flatshareteam.flatsharebackend.listings.dto.ListingFilterCriteria;
import com.flatshareteam.flatsharebackend.listings.model.Listing;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ListingSpecifications {

    public static final String ROOM = "room";
    public static final String APARTMENT = "apartment";
    public static final String CITY = "city";
    public static final String DISTRICT = "district";
    public static final String STREET = "street";
    public static final String APT_NUMBER = "aptNumber";
    public static final String LANDLORD_ROLE = "landlordRole";
    public static final String USER = "user";
    public static final String ID = "id";

    private ListingSpecifications() {}

    public static Specification<Listing> filterBy(ListingFilterCriteria criteria) {
        return (root, query, cb) -> {
            if (criteria == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            boolean hasCity = criteria.city() != null && !criteria.city().isEmpty();
            boolean hasDistrict = criteria.district() != null && !criteria.district().isEmpty();
            boolean hasStreet = criteria.street() != null && !criteria.street().isEmpty();
            boolean hasAptNumber = criteria.aptNumber() != null && !criteria.aptNumber().isEmpty();

            if (hasCity || hasDistrict || hasStreet || hasAptNumber) {
                Join<Object, Object> room = root.join(ROOM);
                Join<Object, Object> apartment = room.join(APARTMENT);

                if (hasCity) {
                    predicates.add(cb.equal(apartment.get(CITY), criteria.city()));
                }
                if (hasDistrict) {
                    predicates.add(cb.equal(apartment.get(DISTRICT), criteria.district()));
                }
                if (hasStreet) {
                    predicates.add(cb.equal(apartment.get(STREET), criteria.street()));
                }
                if (hasAptNumber) {
                    predicates.add(cb.equal(apartment.get(APT_NUMBER), criteria.aptNumber()));
                }
            }

            if (criteria.ownerID() != null) {
                predicates.add(cb.equal(root.join(LANDLORD_ROLE).join(USER).get(ID), criteria.ownerID()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
