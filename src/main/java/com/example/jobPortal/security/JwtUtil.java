package com.example.jobPortal.security;

import com.example.jobPortal.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    private final  String SECRET="supersecretkeysupersecretkeysupersecretkey";

    private final Key key= Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private final long ACCESS_TOKEN_EXPIRY = 1000 * 60 * 15;  // 15 min
    private final long REFRESH_TOKEN_EXPIRY = 1000L * 60 * 60 * 24 * 7; // 7 days

    public String generateAccessToken(String email, UUID userId, Role role){
        return Jwts.builder()
                .setSubject(email)
                .claim("userId",userId.toString())
                .claim("role",role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+ACCESS_TOKEN_EXPIRY))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String email){
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+REFRESH_TOKEN_EXPIRY))
                .signWith(key,SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromToken(String token){
        return parseToken(token).getSubject();
    }

    public Claims parseToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token){
        try{
            parseToken(token);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }


}
