package com.group8.sams.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One score for one student on one assessment. Owner: Member 3.
 *
 * Two business rules are NOT expressible in DDL and are enforced in MarkService
 * (both return 400):
 *   1. marksObtained must be <= assessment.maxMarks. A CHECK constraint can only
 *      see this row, not the parent assessment. The lower bound (>= 0) IS in the
 *      database.
 *   2. enrollment.course must equal assessment.course. Without this you could
 *      record a Physics quiz score against a Chemistry enrollment.
 *
 * No grade or grade-point column exists here: grade is always derived from the
 * percentage by GradeUtil. A stored grade would drift out of sync with the marks
 * that produced it.
 */
@Entity
@Table(name = "marks",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_mark_enrollment_assessment",
           columnNames = {"enrollment_id", "assessment_id"}),
       indexes = {
           @Index(name = "idx_marks_enrollment", columnList = "enrollment_id"),
           @Index(name = "idx_marks_assessment", columnList = "assessment_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entered_by_teacher_id")
    private Teacher enteredBy;

    @Column(name = "marks_obtained", nullable = false, precision = 5, scale = 2)
    private BigDecimal marksObtained;

    @CreationTimestamp
    @Column(name = "entered_at", nullable = false, updatable = false)
    private LocalDateTime enteredAt;
}
