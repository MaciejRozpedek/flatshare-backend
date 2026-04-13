package com.flatshareteam.flatsharebackend.listings.model;

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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
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

    public void addListing(Listing listing) {
        this.listings.add(listing);
        listing.setRoom(this);
    }

    public void removeListing(Listing listing) {
        this.listings.remove(listing);
        listing.setRoom(null);
    }
}
