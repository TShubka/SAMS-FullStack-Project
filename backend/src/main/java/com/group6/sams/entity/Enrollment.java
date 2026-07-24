package com.group6.sams.entity;

import com.group6.sams.entity.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Resolves the many-to-many between Student and Course. Owner: Member 2.
 *
 * This is a first-class entity rather than a plain join table because the
 * relationship carries its own data (semester, academic year, status) and
 * because it owns the attendance and marks records.
 *
 * The composite unique constraint is the duplicate-enrollment guarantee.
 */
@Entity
@Table(name = "enrollments",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_enrollment_student_course_term",
           columnNames = {"student_id", "course_id", "semester", "academic_year"}),
       indexes = {
           @Index(name = "idx_enrollments_student", columnList = "student_id"),
           @Index(name = "idx_enrollments_course", columnList = "course_id"),
           @Index(name = "idx_enrollments_term", columnList = "semester, academic_year")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private Integer semester;

    /** Fixed-width "2025-2026" format, so lexical ordering matches chronological. */
    @Column(name = "academic_year", nullable = false, length = 9)
    private String academicYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @Column(name = "enrolled_on", nullable = false)
    @Builder.Default
    private LocalDate enrolledOn = LocalDate.now();
}
