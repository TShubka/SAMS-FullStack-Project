package com.group6.sams.repository;

import com.group6.sams.entity.Student;
import com.group6.sams.repository.projection.StudentCountByDepartment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUserId(Long userId);

    Optional<Student> findByRollNumber(String rollNumber);

    boolean existsByRollNumber(String rollNumber);

    boolean existsByUserId(Long userId);

    long countByDepartmentId(Long departmentId);

    /**
     * Combined filter + search used by the student list screen. Null parameters are
     * ignored, so one query serves every combination of filters instead of a
     * combinatorial explosion of derived query methods.
     */
    @Query("""
           SELECT s FROM Student s
           WHERE (:departmentId IS NULL OR s.department.id = :departmentId)
             AND (:admissionYear IS NULL OR s.admissionYear = :admissionYear)
             AND (CAST(:search AS string) IS NULL OR
                  LOWER(s.firstName)  LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
                  LOWER(s.lastName)   LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
                  LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
           """)
    Page<Student> findByFilters(@Param("departmentId") Long departmentId,
                                @Param("admissionYear") Integer admissionYear,
                                @Param("search") String search,
                                Pageable pageable);

    List<Student> findByDepartmentId(Long departmentId);

    List<Student> findByDepartmentIdAndAdmissionYear(Long departmentId, Integer admissionYear);

    /** Report: number of students per department, aggregated in the database. */
    @Query("""
           SELECT d.id AS departmentId, d.name AS departmentName, d.code AS departmentCode,
                  COUNT(s) AS studentCount
           FROM Department d LEFT JOIN Student s ON s.department = d
           GROUP BY d.id, d.name, d.code
           ORDER BY d.name
           """)
    List<StudentCountByDepartment> countStudentsByDepartment();
}
