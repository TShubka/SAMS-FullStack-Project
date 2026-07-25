package com.group8.sams.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Student profile. Owner: Member 2.
 *
 * user_id is UNIQUE and NOT NULL, giving the 1:1 with User. Ownership checks
 * ("is this student the caller?") compare student.user.id with the authenticated
 * principal's id.
 */
@Entity
@Table(name = "students",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_students_user", columnNames = "user_id"),
           @UniqueConstraint(name = "uk_students_roll_number", columnNames = "roll_number")
       },
       indexes = {
           @Index(name = "idx_students_department", columnList = "department_id"),
           @Index(name = "idx_students_admission_year", columnList = "admission_year")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "roll_number", nullable = false, length = 20)
    private String rollNumber;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    @Column(name = "current_semester", nullable = false)
    private Integer currentSemester;

    @Column(length = 20)
    private String phone;

    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
