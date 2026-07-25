package com.group8.sams.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {

    private Long id;
    private String code;
    private String title;
    private Integer credits;
    private Integer semester;

    private Long departmentId;
    private String departmentName;

    /** Null when no teacher has been assigned yet. */
    private Long teacherId;
    private String teacherName;
}
