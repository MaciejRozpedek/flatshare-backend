package com.flatshareteam.flatsharebackend.listing.model;

import com.flatshareteam.flatsharebackend.accounts.model.LandlordRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Embedded
    private Money price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_role_id", nullable = false)
    private LandlordRole landlordRole;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Unavailability> unavailabilities = new ArrayList<>();

    public void addUnavailability(Unavailability unavailability) {
        this.unavailabilities.add(unavailability);
        unavailability.setListing(this);
    }

    public void removeUnavailability(Unavailability unavailability) {
        this.unavailabilities.remove(unavailability);
        unavailability.setListing(null);
    }
}
