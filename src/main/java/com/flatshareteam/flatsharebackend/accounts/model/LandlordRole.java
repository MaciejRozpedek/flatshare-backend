package com.flatshareteam.flatsharebackend.accounts.model;

import com.flatshareteam.flatsharebackend.accounts.model.listingModels.Listing;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "landlord_roles")
@Getter
@Setter
public class LandlordRole extends UserRole {

    @OneToMany(mappedBy = "landlordRole", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Listing> listings = new ArrayList<>();

    public LandlordRole() {
        super(RoleType.LANDLORD);
    }
}
