package com.flatshareteam.flatsharebackend.accounts.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "landlord_roles")
@Getter
@Setter
@NoArgsConstructor
public class LandlordRole {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /* * TO DO: Add landlord-specific fields
     * @OneToMany(mappedBy = "landlordRole", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     * private List<Apartment> apartments;
     */
}