package com.group6.sams.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Course offering. Owner: Member 2.
 *
 * teacher is nullable on purpose: a course may exist before anyone is assigned to
 * it. Every service that requires an owning teacher must handle the unassigned
 * case explicitly rather than assuming a teacher is present.
 */
@Entity
@Table(name = "courses",
       uniqueConstraints = @UniqueConstraint(name = "uk_courses_code", columnNames = "code"),
       indexes = {
           @Index(name = "idx_courses_department", columnList = "department_id"),
           @Index(name = "idx_courses_teacher", columnList = "teacher_id"),
           @Index(name = "idx_courses_semester", columnList = "semester")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false)
    private Integer credits;

    @Column(nullable = false)
    private Integer semester;
}
