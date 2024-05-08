package com.clinitalPlatform.security.jwt;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.clinitalPlatform.security.config.AppConfig;
import com.clinitalPlatform.security.oauth2.UserPrincipal;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenProvider {

    private final AppConfig appConfig;


    //--------------ACCESS TOKEN
    public String createToken(Authentication authentication) {
    	
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appConfig.getTokenExpirationMsec());

        return Jwts.builder()
        		.setSubject(userPrincipal.getUsername()) 
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, appConfig.getTokenSecret())
                .compact();
    }

    //--------------REFRESH TOKEN
    public String createRefreshToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appConfig.getRefreshTokenExpirationMsec());

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, appConfig.getTokenSecret())
                .compact();
    }


    //____________________EXTRACTION_________________________________

    //Extraction de l'identifiant du nom_user à partir du jeton JWT
	public String getUserNameFromJwtToken(String token) {
		return Jwts.parser().setSigningKey(appConfig.getTokenSecret()).parseClaimsJws(token).getBody().getSubject();
	}

    //Extraction de l'identifiant du user à partir du jeton JWT
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(appConfig.getTokenSecret())
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    //_____________________________________________________

    //-----------VALIDATE ACCÈSS TOKEN
    public boolean validateAccessToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(appConfig.getTokenSecret()).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }

    //-----------VALIDATE REFRESH TOKEN
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parser().setSigningKey(appConfig.getTokenSecret()).parseClaimsJws(refreshToken);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature for refresh token");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token format for refresh token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT refresh token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT refresh token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty for refresh token.");
        }
        return false;
    }
 //-----------------

}