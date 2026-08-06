package com.saif.contactmanagement.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;



    public String generateToken(UserDetails userDetails) {
        return null;
    }

    public String extractUsername(String token) {
        return null;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return false;
    }

    private boolean isTokenExpired(String token) {
        return false;
    }

    private Date extractExpiration(String token) {
        return null;
    }

    private Claims extractAllClaims(String token) {
        return null;
    }

}