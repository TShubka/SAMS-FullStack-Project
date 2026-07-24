package com.group6.sams.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Teacher profile. Owner: Member 2.
 *
 * The teacher-ownership authorization rule (a teacher may only touch attendance
 * and marks for courses assigned to them) is enforced by comparing
 * course.teacher.user.id with the authenticated principal's id.
 */
@Entity
@Table(name = "teachers",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_teachers_user", columnNames = "user_id"),
           @UniqueConstraint(name = "uk_teachers_employee_code", columnNames = "employee_code")
       },
       indexes = @Index(name = "idx_teachers_department", columnList = "department_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "employee_code", nullable = false, length = 20)
    private String employeeCode;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(length = 50)
    private String designation;

    @Column(length = 20)
    private String phone;

    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
