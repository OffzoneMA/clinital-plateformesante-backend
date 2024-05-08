package com.clinitalPlatform.security.jwt;

import com.clinitalPlatform.enums.TokenType;
import com.clinitalPlatform.models.JwtTokens;
import com.clinitalPlatform.security.config.AppConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {


    @Value("${jwt.secret}")
    private String secret;
    @Autowired
    private AppConfig appConfig;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Boolean validateRefreshToken(String refreshToken) {
        try {
            // Valider le token en utilisant la méthode extractAllClaims
            Claims claims = extractAllClaims(refreshToken);
            // Vérifier si le token est expiré
            return !isTokenExpired(refreshToken);
        } catch (Exception e) {
            // En cas d'erreur de validation, retourner false
            return false;
        }
    }

    //------------------------------------------------------------------------

    public String generateRefreshToken(String userName) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userName, TokenType.REFRESH_TOKEN);
    }

    private String createToken(Map<String, Object> claims, String userName,TokenType tokenType) {
        long expirationTime = (tokenType == TokenType.REFRESH_TOKEN) ? appConfig.getRefreshTokenExpirationMsec() : appConfig.getAccessTokenExpirationMsec();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                //.setExpiration(new Date(System.currentTimeMillis()+1000*60*30))
                //.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 90))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    public JwtTokens generateTokens(String userName){
        Map<String,Object> claims=new HashMap<>();
        String accessToken = createToken(claims, userName, TokenType.ACCESS_TOKEN);
        String refreshToken = createToken(claims, userName, TokenType.REFRESH_TOKEN);
        return new JwtTokens(accessToken, refreshToken);
    }
    private Key getSignKey() {
        byte[] keyBytes= Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //---------------------------------------------------------------------------


   /* public String generateToken(String userName){
        Map<String,Object> claims=new HashMap<>();
        return createToken(claims,userName);
    }*/
   /* private String createToken(Map<String, Object> claims, String userName) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                //.setExpiration(new Date(System.currentTimeMillis()+1000*60*30))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 90))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }*/
   /* private Key getSignKey() {
        byte[] keyBytes= Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }*/
}
