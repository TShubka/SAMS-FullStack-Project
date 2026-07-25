package com.group8.sams.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group8.sams.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Returns 403 when an authenticated user lacks the required role. Owner: Member 1.
 *
 * Distinct from JwtAuthEntryPoint: that one answers "you are nobody" with 401, this
 * one answers "you are somebody, but not somebody allowed to do this" with 403.
 * Keeping them apart is what lets the frontend tell "log in again" apart from
 * "you may not do that", and it is the practical difference between authentication
 * and authorization.
 *
 * Access denied thrown inside a controller is handled by GlobalExceptionHandler;
 * this handler covers denials raised by the filter chain itself.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message("You do not have permission to perform this action")
                .path(request.getRequestURI())
                .build();

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
