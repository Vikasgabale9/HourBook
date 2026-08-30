package com.app.hourbook.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.app.hourbook.enums.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateToken(Long id,
                                String entity,
                                String identifier,
                                Role role) {

        return Jwts.builder()
                .subject(identifier)
                .claim("id", id)
                .claim("entity", entity)
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractIdentifier(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractId(String token) {
        return extractAllClaims(token).get("id", Long.class);
    }

    public String extractEntity(String token) {
        return extractAllClaims(token).get("entity", String.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {

        try {

            Claims claims = extractAllClaims(token);

            return claims.getExpiration().after(new Date());

        } catch (SignatureException e) {

            log.error("Invalid JWT Signature : {}", e.getMessage());

        } catch (MalformedJwtException e) {

            log.error("Malformed JWT : {}", e.getMessage());

        } catch (ExpiredJwtException e) {

            log.error("JWT Expired : {}", e.getMessage());

        } catch (UnsupportedJwtException e) {

            log.error("Unsupported JWT : {}", e.getMessage());

        } catch (IllegalArgumentException e) {

            log.error("JWT is empty : {}", e.getMessage());

        } catch (JwtException e) {

            log.error("JWT Validation Failed : {}", e.getMessage());

        }

        return false;
    }

    public String extractToken(HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }

        return header.substring(7);
    }

    public Long getVendorId(HttpServletRequest request) {

        String token = extractToken(request);

        if (token == null) {
            return null;
        }

        
        return extractId(token);
    }
}