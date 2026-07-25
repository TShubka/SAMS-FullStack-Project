package com.group8.sams.entity;

import com.group8.sams.entity.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One attendance record for one student, one course, one date. Owner: Member 3.
 *
 * Attaching to Enrollment rather than to (student, course) separately makes it
 * structurally impossible to record attendance for a student who is not enrolled
 * in the course - the foreign key alone enforces it.
 *
 * recordedBy is an audit trail. It is nullable and ON DELETE SET NULL so that
 * removing a teacher does not destroy academic history; we lose only attribution.
 */
@Entity
@Table(name = "attendance",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_attendance_enrollment_date",
           columnNames = {"enrollment_id", "attendance_date"}),
       indexes = {
           @Index(name = "idx_attendance_enrollment", columnList = "enrollment_id"),
           @Index(name = "idx_attendance_date", columnList = "attendance_date")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_teacher_id")
    private Teacher recordedBy;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AttendanceStatus status;

    @Column(length = 255)
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
