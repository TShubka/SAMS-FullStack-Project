package com.group6.sams.entity;

import com.group6.sams.entity.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

/**
 * Reference data - four rows, created by DataSeeder, never edited through the API.
 * Owner: Member 1.
 */
@Entity
@Table(name = "roles",
       uniqueConstraints = @UniqueConstraint(name = "uk_roles_name", columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RoleName name;

    public Role(RoleName name) {
        this.name = name;
    }
}
