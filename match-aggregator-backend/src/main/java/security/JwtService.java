package ru.mirea.matchaggregator.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import ru.mirea.matchaggregator.config.AppProperties;
import ru.mirea.matchaggregator.domain.UserAccount;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final int accessMinutes;

    public JwtService(AppProperties props) {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(props.jwt().secretBase64());
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.issuer = props.jwt().issuer();
        this.accessMinutes = props.jwt().accessMinutes();
    }

    public String generateAccessToken(UserAccount user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessMinutes, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    public String generateRefreshTokenValue() {
        return UUID.randomUUID().toString() + "." + UUID.randomUUID().toString();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public int getAccessMinutes() { return accessMinutes; }
}
