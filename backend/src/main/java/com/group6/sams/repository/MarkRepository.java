package com.group6.sams.repository;

import com.group6.sams.entity.Mark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarkRepository extends JpaRepository<Mark, Long> {

    boolean existsByEnrollmentIdAndAssessmentId(Long enrollmentId, Long assessmentId);

    Optional<Mark> findByEnrollmentIdAndAssessmentId(Long enrollmentId, Long assessmentId);

    List<Mark> findByEnrollmentId(Long enrollmentId);

    List<Mark> findByEnrollmentCourseId(Long courseId);

    List<Mark> findByAssessmentId(Long assessmentId);

    List<Mark> findByEnrollmentStudentId(Long studentId);
}
