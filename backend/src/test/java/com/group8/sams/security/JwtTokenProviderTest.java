package com.group8.sams.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for token generation and validation. No database and no Spring
 * context - this is pure logic, so it is cheap to test and fast to run.
 *
 * These cover the claims the viva asks about: what a JWT carries, how tampering is
 * detected, and how expiry is enforced.
 */
class JwtTokenProviderTest {

    private static final String SECRET =
            "test-secret-key-that-is-definitely-long-enough-for-hs256";
    private static final long ONE_HOUR = 3_600_000L;

    private final JwtTokenProvider provider = new JwtTokenProvider(SECRET, ONE_HOUR);

    private UserPrincipal principal() {
        return new UserPrincipal(42L, "alice", "alice@example.com", "hashed", true,
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"),
                        new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("A generated token is valid and round-trips the username and user id")
    void generatesValidToken() {
        String token = provider.generateToken(principal());

        assertTrue(provider.validateToken(token));
        assertEquals("alice", provider.getUsernameFromToken(token));
        assertEquals(42L, provider.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("A token has three dot-separated parts: header, payload, signature")
    void tokenHasThreeParts() {
        assertEquals(3, provider.generateToken(principal()).split("\\.").length);
    }

    @Test
    @DisplayName("Tampering with the payload invalidates the signature")
    void rejectsTamperedToken() {
        String token = provider.generateToken(principal());
        String[] parts = token.split("\\.");

        // Swap the payload for a different one, leaving the original signature.
        String tampered = parts[0] + "." + parts[1].substring(0, parts[1].length() - 4)
                          + "AAAA." + parts[2];

        assertFalse(provider.validateToken(tampered),
                "A token whose payload was altered must fail signature verification");
    }

    @Test
    @DisplayName("A token signed with a different secret is rejected")
    void rejectsTokenFromForeignSecret() {
        JwtTokenProvider attacker = new JwtTokenProvider(
                "a-completely-different-secret-key-also-long-enough!!", ONE_HOUR);
        String forged = attacker.generateToken(principal());

        assertFalse(provider.validateToken(forged),
                "Our server must not accept a token it did not sign");
    }

    @Test
    @DisplayName("An expired token is rejected")
    void rejectsExpiredToken() {
        // Negative lifetime: the token is already past its expiry when created.
        JwtTokenProvider expiring = new JwtTokenProvider(SECRET, -1000L);
        String token = expiring.generateToken(principal());

        assertFalse(expiring.validateToken(token));
    }

    @Test
    @DisplayName("Malformed, empty and null tokens are rejected without throwing")
    void rejectsGarbage() {
        assertFalse(provider.validateToken("not-a-jwt"));
        assertFalse(provider.validateToken(""));
        assertFalse(provider.validateToken(null));
    }

    @Test
    @DisplayName("A secret shorter than 32 bytes is refused at construction")
    void refusesWeakSecret() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new JwtTokenProvider("too-short", ONE_HOUR));
        assertTrue(ex.getMessage().contains("32 bytes"));
    }
}
