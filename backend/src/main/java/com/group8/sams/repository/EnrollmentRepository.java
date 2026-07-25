package com.group8.sams.repository;

import com.group8.sams.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /**
     * The service-level duplicate check. The composite unique constraint on the
     * table is the real guarantee; this exists so the user gets a clean 409 with a
     * readable message instead of a raw constraint violation surfacing as a 500.
     */
    boolean existsByStudentIdAndCourseIdAndSemesterAndAcademicYear(
            Long studentId, Long courseId, Integer semester, String academicYear);

    Optional<Enrollment> findByStudentIdAndCourseIdAndSemesterAndAcademicYear(
            Long studentId, Long courseId, Integer semester, String academicYear);

    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByCourseId(Long courseId);

    Page<Enrollment> findByStudentId(Long studentId, Pageable pageable);

    Page<Enrollment> findByCourseId(Long courseId, Pageable pageable);

    List<Enrollment> findByStudentIdAndSemesterAndAcademicYear(
            Long studentId, Integer semester, String academicYear);

    long countByCourseId(Long courseId);

    boolean existsByCourseId(Long courseId);

    boolean existsByStudentId(Long studentId);
}
