package com.pdgigs.infrastructure.adapter.output.persistence;

import com.pdgigs.domain.model.User;
import com.pdgigs.domain.port.output.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProviderAdapter implements JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenProviderAdapter(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    @Override
    public Mono<String> generateToken(User user) {
        return Mono.fromCallable(() -> {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.id());
            claims.put("email", user.email());
            claims.put("name", user.name());
            claims.put("role", user.role());

            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expirationMs);

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.email())
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(secretKey)
                    .compact();
        });
    }

    @Override
    public Mono<String> extractEmail(String token) {
        return Mono.fromCallable(() -> extractAllClaims(token).getSubject());
    }

    @Override
    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                Claims claims = extractAllClaims(token);
                return !claims.getExpiration().before(new Date());
            } catch (Exception e) {
                log.debug("Invalid JWT token: {}", e.getMessage());
                return false;
            }
        });
    }

    @Override
    public Mono<String> extractRole(String token) {
        return Mono.fromCallable(() -> extractAllClaims(token).get("role", String.class));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}