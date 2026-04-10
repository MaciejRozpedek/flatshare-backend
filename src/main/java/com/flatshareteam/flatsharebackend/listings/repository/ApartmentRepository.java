package com.flatshareteam.flatsharebackend.listings.repository;

import com.flatshareteam.flatsharebackend.listings.model.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {
    Optional<Apartment> findById(UUID uuid);
    Optional<Apartment> findByCityAndDistrictAndStreetAndAptNumber(String city, String district, String street, String aptNumber);
}
