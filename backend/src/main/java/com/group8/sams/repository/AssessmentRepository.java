package com.group8.sams.repository;

import com.group8.sams.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findByCourseId(Long courseId);

    boolean existsByCourseIdAndTitle(Long courseId, String title);

    /**
     * Supports the "weights must not exceed 100 percent" rule, which cannot be a
     * CHECK constraint because a row-level check cannot see sibling rows.
     * excludeId lets an update exclude the row being edited from the total.
     */
    @Query("""
           SELECT COALESCE(SUM(a.weightPercent), 0)
           FROM Assessment a
           WHERE a.course.id = :courseId
             AND (:excludeId IS NULL OR a.id <> :excludeId)
           """)
    BigDecimal sumWeightByCourse(@Param("courseId") Long courseId,
                                 @Param("excludeId") Long excludeId);
}
