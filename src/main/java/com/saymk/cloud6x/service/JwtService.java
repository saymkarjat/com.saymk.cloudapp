package com.saymk.cloud6x.service;

import com.saymk.cloud6x.exception.InvalidJwtTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.access_token_expiration_minutes}")
    private int accessTokenExpirationMinutes;

    @Value("${jwt.refresh_token_expiration_days}")
    private int refreshTokenExpirationDays;

    public String generateAccessToken(String username) {
        long millis = TimeUnit.MINUTES.toMillis(accessTokenExpirationMinutes);
        return generateToken(username, millis);
    }

    public String generateRefreshToken(String username) {
        long millis = TimeUnit.DAYS.toMillis(refreshTokenExpirationDays);
        return generateToken(username, millis);
    }

    public boolean isValid(String token, UserDetails user) {
        try {
            String username = user.getUsername();
            return username.equals(extractUsername(token)) && !isTokenExpired(token);
        } catch (InvalidJwtTokenException e) {
            return false;
        }
    }


    public boolean isTokenExpired(String token) {
        return Instant.now().isAfter(extractExpiration(token));
    }

    public Instant extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration).toInstant();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            Claims claims = Jwts
                    .parser()
                    .verifyWith(getSecretKey(SECRET))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            throw new InvalidJwtTokenException("Invalid token");
        }
    }

    private String generateToken(String username, long expirationTime) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSecretKey(SECRET))
                .compact();
    }

    private SecretKey getSecretKey(String secret) {
        byte[] bytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(bytes);
    }
}
