package com.group6.sams.security;

import com.group6.sams.entity.User;
import com.group6.sams.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads the account Spring Security authenticates against. Owner: Member 1.
 *
 * Roles are EAGER on User, so the authorities are populated here without an extra
 * query and without risking a LazyInitializationException once the transaction ends.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No account found for username: " + username));
        return UserPrincipal.from(user);
    }

    /** Used by the JWT filter, which carries the user id rather than the name. */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No account found for id: " + id));
        return UserPrincipal.from(user);
    }
}
