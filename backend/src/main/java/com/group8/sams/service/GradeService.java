package com.group8.sams.service;

import com.group8.sams.dto.response.CourseGradeResponse;
import com.group8.sams.dto.response.GpaResponse;
import com.group8.sams.security.UserPrincipal;

import java.util.List;

public interface GradeService {

    /** Computed result for one enrollment. */
    CourseGradeResponse gradeForEnrollment(Long enrollmentId, UserPrincipal caller);

    /** Every course result for a student. */
    List<CourseGradeResponse> gradesForStudent(Long studentId, UserPrincipal caller);

    /**
     * Semester GPA when semester and academicYear are given, cumulative GPA when
     * they are null.
     */
    GpaResponse gpa(Long studentId, Integer semester, String academicYear,
                    UserPrincipal caller);
}
