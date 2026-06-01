package com.flatshareteam.flatsharebackend.accounts.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "admin_roles")
@Getter
@Setter
public class AdminRole extends UserRole {
    public AdminRole() {
        super(RoleType.ADMIN);
    }
}
