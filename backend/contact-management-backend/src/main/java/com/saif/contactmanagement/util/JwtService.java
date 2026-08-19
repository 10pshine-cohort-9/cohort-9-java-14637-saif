package com.saif.contactmanagement.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import com.saif.contactmanagement.service.impl.CustomUserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(UserDetails userDetails) {
        int version = 1;
        if (userDetails instanceof CustomUserDetails) {
            version = ((CustomUserDetails) userDetails).getCredentialVersion();
        }
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("version", version)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        Integer tokenVersion = extractAllClaims(token).get("version", Integer.class);
        int currentVersion = 1;
        if (userDetails instanceof CustomUserDetails) {
            currentVersion = ((CustomUserDetails) userDetails).getCredentialVersion();
        }
        return extractUsername(token).equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && (tokenVersion != null && tokenVersion == currentVersion);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getJwtExpiration() {
        return jwtExpiration;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}