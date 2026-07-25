package com.group8.sams.entity;

import com.group8.sams.entity.enums.AssessmentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A graded item within a course (assignment, quiz, midterm, final). Owner: Member 3.
 *
 * Business rule NOT expressible in DDL: the sum of weightPercent across a course's
 * assessments must not exceed 100. A row-level CHECK constraint cannot see sibling
 * rows, so AssessmentService enforces this and returns 400.
 */
@Entity
@Table(name = "assessments",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_assessment_course_title",
           columnNames = {"course_id", "title"}),
       indexes = @Index(name = "idx_assessments_course", columnList = "course_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssessmentType type;

    @Column(name = "max_marks", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxMarks;

    @Column(name = "weight_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightPercent;

    @Column(name = "assessed_on")
    private LocalDate assessedOn;
}
