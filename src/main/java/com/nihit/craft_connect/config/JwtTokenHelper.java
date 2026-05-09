package com.nihit.craft_connect.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenHelper {
//    public static final long JWT_TOKEN_VALIDITY = 5L * 60000 * 60000;
    private String secret = "eyJhbGciOiJIUzUxMiJ9eyJzdWIiOiJjaGVja0BnbWFpbC5jb20iLCJpYXQiOjE2NzUxNDg2MzIsImV4cCI6MTY3ODg4MDkwOX0cpvMGTr8f9kboyNg6E563yRKoijj4S5iCWcuOkSGrGmxSJpO9CsVijqrWalo3gRDQSrmxjGj8bxF3WNXJgBQ";

    //retrieve username from jwt token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    public String extractUsernameFromToken(String theToken){
        return extractClaim(theToken, Claims ::getSubject);
    }
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignedKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

    }
    //retrieve expiration date from jwt token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    public String extractUserTypeFromToken(String theToken) {
        return extractClaim(theToken, claims -> claims.get("userType", String.class));
    }
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignedKey())
                .build()
                .parseClaimsJws(token)
                .getBody();}


    private Key getSignedKey(){
        byte[] keyByte = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyByte);
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }


    public String generateToken(String userName, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userType", userType); // Add user type to claims
        return doGenerateToken(claims, userName);
    }
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        long expirationTimeInMilliseconds = 5 * 60 * 60 * 1000;
        Date expirationDate = new Date(System.currentTimeMillis() + expirationTimeInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationDate)
                .signWith(getSignedKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    //validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }}