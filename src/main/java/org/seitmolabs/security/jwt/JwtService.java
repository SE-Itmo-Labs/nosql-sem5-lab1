package org.seitmolabs.security.jwt;

import java.security.Key;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;

@Service
public abstract class JwtService {
    
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateToken() {return "";}

    public String extractUsername() {return "";}

    public boolean isValidToken() {return false;}

    // сделать private потом
    public abstract Claims extractAlClaims();

    // сделать private потом
    public abstract Key getSigningKey();
}
