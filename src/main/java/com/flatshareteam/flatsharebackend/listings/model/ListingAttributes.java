package com.flatshareteam.flatsharebackend.listings.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingAttributes {
    private boolean petsAllowed;
    private boolean nonSmokingOnly;
    private boolean closeToShops;
    private String profile;
}