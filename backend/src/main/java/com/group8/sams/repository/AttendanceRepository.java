package com.group8.sams.repository;

import com.group8.sams.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsByEnrollmentIdAndAttendanceDate(Long enrollmentId, LocalDate date);

    Optional<Attendance> findByEnrollmentIdAndAttendanceDate(Long enrollmentId, LocalDate date);

    List<Attendance> findByEnrollmentId(Long enrollmentId);

    List<Attendance> findByEnrollmentStudentId(Long studentId);

    List<Attendance> findByEnrollmentCourseIdAndAttendanceDate(Long courseId, LocalDate date);

    List<Attendance> findByEnrollmentCourseId(Long courseId);

    /**
     * Attendance percentage for one enrollment, computed in the database.
     * LATE is counted as present, matching app.attendance.late-counts-as-present.
     * Returns null when the enrollment has no attendance rows at all - callers must
     * treat "no records" as "no percentage", not as zero.
     */
    @Query("""
           SELECT (SUM(CASE WHEN a.status IN (com.group8.sams.entity.enums.AttendanceStatus.PRESENT,
                                              com.group8.sams.entity.enums.AttendanceStatus.LATE)
                            THEN 1.0 ELSE 0.0 END) * 100.0) / COUNT(a)
           FROM Attendance a
           WHERE a.enrollment.id = :enrollmentId
           """)
    Double calculatePercentageByEnrollment(@Param("enrollmentId") Long enrollmentId);

    long countByEnrollmentId(Long enrollmentId);

    /** Backs the SET NULL behaviour when a teacher is deleted. */
    List<Attendance> findByRecordedById(Long teacherId);
}
