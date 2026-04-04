package com.flatshareteam.flatsharebackend.accounts.model.listingModels;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Float area;

    @Enumerated(EnumType.STRING)
    @Column
    private RoomStatus status;

    @Embedded
    private Money pricePerMonth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<Listing> listings = new java.util.ArrayList<>();
}
