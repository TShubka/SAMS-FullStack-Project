package com.group6.sams.repository;

import com.group6.sams.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
             AND (:search IS NULL OR
                  LOWER(s.firstName)  LIKE LOWER(CONCAT('%', :search, '%')) OR
                  LOWER(s.lastName)   LIKE LOWER(CONCAT('%', :search, '%')) OR
                  LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :search, '%')))
           """)
    Page<Student> findByFilters(@Param("departmentId") Long departmentId,
                                @Param("admissionYear") Integer admissionYear,
                                @Param("search") String search,
                                Pageable pageable);
}
