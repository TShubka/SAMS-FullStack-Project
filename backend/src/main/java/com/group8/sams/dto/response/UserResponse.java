package com.group8.sams.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User representation returned by the API.
 *
 * There is deliberately no password field. Returning the entity directly would
 * expose the hash; this DTO makes that impossible by construction rather than by
 * remembering to annotate it away.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private Boolean enabled;
    private List<String> roles;
    private LocalDateTime createdAt;
}
