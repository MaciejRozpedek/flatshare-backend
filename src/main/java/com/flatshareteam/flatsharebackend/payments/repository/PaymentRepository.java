package com.flatshareteam.flatsharebackend.payments.repository;

import com.flatshareteam.flatsharebackend.payments.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByProviderReference(String providerReference);
    java.util.List<Payment> findByBookingId(UUID bookingId);
}
