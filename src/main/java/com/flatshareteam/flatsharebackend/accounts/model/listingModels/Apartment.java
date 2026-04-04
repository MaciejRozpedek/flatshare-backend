package com.flatshareteam.flatsharebackend.accounts.model.listingModels;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "apartments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Room> rooms = new ArrayList<>();
}
