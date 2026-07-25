package com.group8.sams.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * The security chain. Owner: Member 1.
 *
 * Phase 3 replaces the Phase 2 placeholder. Design points:
 *
 *  - STATELESS: no HTTP session is ever created. Identity comes from the token on
 *    every request, which is what makes the API horizontally scalable and what
 *    "stateless authentication" actually means.
 *  - CSRF disabled: CSRF attacks rely on the browser automatically attaching
 *    credentials (cookies). We use a bearer token that JavaScript must attach
 *    deliberately, so there is nothing for a cross-site form post to abuse.
 *  - BCrypt: a deliberately slow, salted adaptive hash. Each password gets a unique
 *    salt automatically, so identical passwords produce different hashes and
 *    precomputed rainbow tables are useless.
 *  - @EnableMethodSecurity turns on @PreAuthorize, used on controllers for coarse
 *    role checks. Fine-grained ownership rules ("this teacher's own course", "this
 *    student's own record") live in the services, because only the service can load
 *    the record and compare it against the caller.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthEntryPoint authEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthEntryPoint authEntryPoint,
                          JwtAccessDeniedHandler accessDeniedHandler,
                          CustomUserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authEntryPoint = authEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authEntryPoint)      // 401
                    .accessDeniedHandler(accessDeniedHandler))     // 403
            .authorizeHttpRequests(auth -> auth
                    // Only registration and login are public. /api/auth/** as a
                    // whole would also expose /api/auth/me, which needs a session -
                    // an unauthenticated call would then reach the controller with a
                    // null principal and fail as a 500 instead of a clean 401.
                    .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                    .requestMatchers("/api/auth/me").authenticated()
                    .requestMatchers("/api/health").permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // Course-structure writes are administrative.
                    .requestMatchers(HttpMethod.POST,   "/api/departments/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT,    "/api/departments/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/departments/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST,   "/api/students/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT,    "/api/students/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/students/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST,   "/api/teachers/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT,    "/api/teachers/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/teachers/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST,   "/api/courses/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT,    "/api/courses/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST,   "/api/enrollments/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT,    "/api/enrollments/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/enrollments/**").hasRole("ADMIN")

                    // Attendance and marks are written by teachers and admins only.
                    // This is the rule that stops a student altering their own record;
                    // the owning-teacher check is enforced again in the services.
                    .requestMatchers(HttpMethod.POST,   "/api/attendance/**").hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers(HttpMethod.PUT,    "/api/attendance/**").hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/attendance/**").hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers(HttpMethod.POST,   "/api/assessments/**").hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers(HttpMethod.PUT,    "/api/assessments/**").hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/assessments/**").hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers(HttpMethod.POST,   "/api/marks/**").hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers(HttpMethod.PUT,    "/api/marks/**").hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/marks/**").hasAnyRole("TEACHER", "ADMIN")

                    .requestMatchers("/api/users/me").authenticated()
                    .requestMatchers("/api/users/**").hasRole("ADMIN")

                    .requestMatchers("/api/dashboard/admin").hasRole("ADMIN")
                    .requestMatchers("/api/dashboard/teacher").hasRole("TEACHER")
                    .requestMatchers("/api/dashboard/student").hasRole("STUDENT")

                    // Default deny. Anything not matched above still requires a valid
                    // token, so a forgotten endpoint fails closed, not open.
                    .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter,
                             UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
