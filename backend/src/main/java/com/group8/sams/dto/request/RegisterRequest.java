package com.group8.sams.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Registration payload. Owner: Member 1.
 *
 * Validation lives here as annotations rather than as if-statements in the
 * controller, so failures are reported uniformly by GlobalExceptionHandler as a
 * 400 with a field -> message map the React forms can render inline.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
             message = "Username may contain only letters, digits, dot, underscore and hyphen")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    @Size(max = 120, message = "Email must not exceed 120 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
    private String password;

    /**
     * Optional. Accepts ADMIN, TEACHER or STUDENT. Defaults to STUDENT when absent.
     *
     * Self-registering as ADMIN through a public endpoint would be a privilege
     * escalation hole, so AuthService rejects any attempt to register as ADMIN.
     */
    @Pattern(regexp = "^(ADMIN|TEACHER|STUDENT)$",
             message = "Role must be one of ADMIN, TEACHER or STUDENT")
    private String role;
}
