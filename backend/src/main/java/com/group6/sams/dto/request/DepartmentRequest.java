package com.group6.sams.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentRequest {

    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Department code is required")
    @Size(max = 10, message = "Code must not exceed 10 characters")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Code must be letters and digits only")
    private String code;
}
