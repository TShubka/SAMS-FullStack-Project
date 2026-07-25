package com.group8.sams.repository;

import com.group8.sams.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByUserId(Long userId);

    Optional<Teacher> findByEmployeeCode(String employeeCode);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByUserId(Long userId);

    long countByDepartmentId(Long departmentId);

    @Query("""
           SELECT t FROM Teacher t
           WHERE (:departmentId IS NULL OR t.department.id = :departmentId)
             AND (CAST(:search AS string) IS NULL OR
                  LOWER(t.firstName)    LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
                  LOWER(t.lastName)     LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR
                  LOWER(t.employeeCode) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
           """)
    Page<Teacher> findByFilters(@Param("departmentId") Long departmentId,
                                @Param("search") String search,
                                Pageable pageable);
}
