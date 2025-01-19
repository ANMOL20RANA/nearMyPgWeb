package com.nearPg.Authentication_NearPg.JwtSupport_TO_0TP.Config;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.nearPg.Authentication_NearPg.repository.UsersRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {
    // 5E3F7A8A3E1B9D45A7C54D2EAE0C6B3F5D9E5D6A7C8F1A2B3D4E5F6A7B8C9D0E

    private UsersRepository userRepo;
    private static final String SECRET_KEY = "WTVv9FzZhD7UnCv9Lyrzxl6hG5XBwm5Q5eMKU3xJ27Y";

    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return isTokenexpired(token).before(new Date());
    }

    private Date isTokenexpired(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    public <T> T extractClaims(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClims(token);
        return claimResolver.apply(claims);
    }

    public Claims extractAllClims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateToken(UserDetails userDetails) {
        System.out.println(userDetails);
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraclaim, UserDetails userDetails) {
        return Jwts
                .builder()
                .setClaims(extraclaim)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 700000))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignKey() {
        byte[] keybyte = Decoders.BASE64URL.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keybyte);
    }

}
