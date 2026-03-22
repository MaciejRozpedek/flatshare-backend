package com.flatshareteam.flatsharebackend.accounts.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tenant_roles")
@Getter
@Setter
public class TenantRole extends UserRole {

// TO DO: Add tenant-specific fields
//    @Column(name = "max_price")
//    private BigDecimal maxPrice;
//
//    @Column(length = 3)
//    private String currency;
//
//    @Column(name = "smoking_allowed")
//    private Boolean smokingAllowed;
//
//    @Column(name = "pets_allowed")
//    private Boolean petsAllowed;
//
//    @ElementCollection(fetch = FetchType.LAZY)
//    @CollectionTable(
//        name = "tenant_preferred_districts",
//        joinColumns = @JoinColumn(name = "tenant_role_id")
//    )
//    @Column(name = "district")
//    private Set<String> preferredDistricts;

    public TenantRole() {
        super(RoleType.TENANT);
    }
}
