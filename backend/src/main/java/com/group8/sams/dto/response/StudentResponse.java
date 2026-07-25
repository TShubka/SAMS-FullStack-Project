package com.group8.sams.dto.response;

import lombok.*;

/**
 * Flattens the department and user references into plain fields.
 *
 * Returning the Student entity instead would drag lazy proxies into Jackson and
 * expose the user's password hash - this DTO makes both impossible.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {

    private Long id;
    private String rollNumber;
    private String firstName;
    private String lastName;
    private String fullName;
    private Integer admissionYear;
    private Integer currentSemester;
    private String phone;

    private Long departmentId;
    private String departmentName;
    private String departmentCode;

    private Long userId;
    private String username;
    private String email;
}
