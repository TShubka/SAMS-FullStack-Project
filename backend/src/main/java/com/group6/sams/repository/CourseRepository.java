package com.group6.sams.repository;

import com.group6.sams.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCode(String code);

    boolean existsByCode(String code);

    long countByDepartmentId(Long departmentId);

    /** Backs GET /api/courses/my - the courses assigned to the calling teacher. */
    List<Course> findByTeacherId(Long teacherId);

    boolean existsByTeacherId(Long teacherId);

    @Query("""
           SELECT c FROM Course c
           WHERE (:departmentId IS NULL OR c.department.id = :departmentId)
             AND (:semester     IS NULL OR c.semester = :semester)
             AND (:teacherId    IS NULL OR c.teacher.id = :teacherId)
             AND (:search       IS NULL OR
                  LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
                  LOWER(c.code)  LIKE LOWER(CONCAT('%', :search, '%')))
           """)
    Page<Course> findByFilters(@Param("departmentId") Long departmentId,
                               @Param("semester") Integer semester,
                               @Param("teacherId") Long teacherId,
                               @Param("search") String search,
                               Pageable pageable);
}
