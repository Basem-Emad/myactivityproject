package com.raya.activitytracking.usermanagement.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // Base64 encoded secret key
    private static final String SECRET_KEY =
            "VGhpc0lzQVNlY3VyZVNlY3JldEtleUZvckpXVFRlc3QxMjM0NTY3ODkw";

    // Token expiration: 24 hours
    private static final long JWT_EXPIRATION = 1000 * 60 * 60 * 24;

    /**
     * Generate JWT token for authenticated user
     */
    public String generateToken(UserDetails userDetails) {

        CustomUserDetails customUserDetails =
                (CustomUserDetails) userDetails;

        return Jwts.builder()
                .subject(userDetails.getUsername())

                .claim(
                        "permissions",
                        customUserDetails.getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList()
                )

                .issuedAt(new Date(System.currentTimeMillis()))

                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + JWT_EXPIRATION
                        )
                )

                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract username from JWT
     */
    public String extractUsername(String token) {

        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract any claim from JWT
     */
    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {

        final Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims
     */
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if JWT is valid
     */
    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        final String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    /**
     * Check token expiration
     */
    private boolean isTokenExpired(String token) {

        return extractExpiration(token).before(new Date());
    }

    /**
     * Extract expiration date
     */
    private Date extractExpiration(String token) {

        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * JWT signing key
     */
    private SecretKey getSigningKey() {

        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
