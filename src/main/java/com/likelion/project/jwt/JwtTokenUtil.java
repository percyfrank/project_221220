package com.likelion.project.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtTokenUtil {

    public static String getUserName(String token, String key) {
        return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody()
                .get("userName",String.class);
    }
    public static boolean isExpired(String token, String key) {
        Date expiredDate = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody()
                .getExpiration();
        return expiredDate.before(new Date());

    }

    public static String createToken(String userName, String secretKey, long expireTimeMs) {

        Claims claims = Jwts.claims();
        claims.put("userName", userName);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireTimeMs))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
}
