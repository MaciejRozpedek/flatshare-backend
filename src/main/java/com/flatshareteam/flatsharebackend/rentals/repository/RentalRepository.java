package com.flatshareteam.flatsharebackend.rentals.repository;

import com.flatshareteam.flatsharebackend.rentals.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RentalRepository extends JpaRepository<Rental, UUID> {
    Optional<Rental> findByIdAndListingLandlordRoleUserId(UUID rentalId, UUID userId);
}
