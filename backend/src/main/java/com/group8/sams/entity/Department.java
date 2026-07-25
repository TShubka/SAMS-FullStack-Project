package com.group8.sams.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Academic department. Owner: Member 2.
 *
 * No child collections are mapped: students, teachers and courses are always
 * queried through their own repositories. That keeps this entity cheap to load
 * and avoids accidental cascade behaviour.
 */
@Entity
@Table(name = "departments",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_departments_name", columnNames = "name"),
           @UniqueConstraint(name = "uk_departments_code", columnNames = "code")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 10)
    private String code;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
