package com.group8.sams.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Generates and validates JSON Web Tokens. Owner: Member 1.
 *
 * A JWT has three dot-separated parts: header, payload (claims) and signature.
 * We sign with HMAC-SHA256 using a server-side secret. The signature is what makes
 * the token tamper-evident: change any character of the payload and verification
 * fails, because the attacker cannot recompute the signature without the secret.
 *
 * The token is NOT encrypted - anyone can decode the payload. So it carries only
 * the user id, username and roles, never a password or anything sensitive.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_ROLES = "roles";

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret,
                            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // HS256 requires a key of at least 256 bits. Failing loudly at startup is
            // far better than issuing weakly-signed tokens in production.
            throw new IllegalArgumentException(
                    "app.jwt.secret must be at least 32 bytes for HS256 (was "
                    + keyBytes.length + ")");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(UserPrincipal principal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        List<String> roles = principal.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        return Jwts.builder()
                .subject(principal.getUsername())
                .claim(CLAIM_USER_ID, principal.getId())
                .claim(CLAIM_ROLES, roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return parseClaims(token).get(CLAIM_USER_ID, Number.class).longValue();
    }

    /**
     * Returns true only for a token that is well-formed, correctly signed and
     * unexpired. Each failure mode is logged distinctly - during Phase 3 testing we
     * need to tell "expired" apart from "tampered".
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Rejected JWT: expired");
        } catch (SignatureException e) {
            log.warn("Rejected JWT: signature mismatch - token may have been tampered with");
        } catch (MalformedJwtException e) {
            log.debug("Rejected JWT: malformed");
        } catch (UnsupportedJwtException e) {
            log.debug("Rejected JWT: unsupported");
        } catch (IllegalArgumentException e) {
            log.debug("Rejected JWT: empty or null");
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
