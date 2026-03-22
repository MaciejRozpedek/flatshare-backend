package com.flatshareteam.flatsharebackend.accounts.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "landlord_roles")
@Getter
@Setter
public class LandlordRole extends UserRole {

    /* * TO DO: Add landlord-specific fields
     * @OneToMany(mappedBy = "landlordRole", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     * private List<Apartment> apartments;
     */

    public LandlordRole() {
        super(RoleType.LANDLORD);
    }
}
