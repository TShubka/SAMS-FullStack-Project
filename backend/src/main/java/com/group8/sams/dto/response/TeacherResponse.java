package com.group8.sams.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherResponse {

    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private String designation;
    private String phone;

    private Long departmentId;
    private String departmentName;

    private Long userId;
    private String username;
    private String email;
}
