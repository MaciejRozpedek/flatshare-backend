package com.flatshareteam.flatsharebackend.accounts.model;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_role_id", nullable = false, unique = true)
    private TenantRole tenantRole;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxPrice;

    @Column(length = 3)
    private String currency;

    @Column
    private Boolean smokingAllowed;

    @Column
    private Boolean petsAllowed;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tenant_preferred_districts", joinColumns = @JoinColumn(name = "user_preferences_id"))
    @Column(name = "district")
    @Builder.Default
    private List<String> preferredDistricts = new ArrayList<>();
}