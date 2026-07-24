package com.group6.sams.repository;

import com.group6.sams.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByName(String name);

    @Query("""
           SELECT d FROM Department d
           WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(d.code) LIKE LOWER(CONCAT('%', :search, '%'))
           """)
    Page<Department> search(@Param("search") String search, Pageable pageable);
}
