package com.group8.sams.dto.response.dashboard;

import com.group8.sams.dto.response.CourseResponse;
import lombok.*;

import java.util.List;

/** Teacher dashboard - scoped to the courses assigned to the caller. Owner: Member 4. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherDashboardResponse {

    private String teacherName;
    private String employeeCode;
    private String departmentName;

    private long assignedCourses;
    private long totalStudents;
    private long lowAttendanceStudents;

    private List<CourseResponse> courses;
}
