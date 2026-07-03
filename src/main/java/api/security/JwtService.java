package api.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String gerarToken(String email, String role){

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getChave())
                .compact();

    }

    public String extrairEmail(String token){
        return extrairClaims(token).getSubject();
    }

    public String extrairRole(String token){
        return extrairClaims(token).get("role", String.class);
    }

    public boolean tokenValido(String token){

        try{
            extrairClaims(token);
            return true;
        }catch(Exception e){
            return false;
        }

    }

    private Claims extrairClaims(String token){

        return Jwts.parser()
        .verifyWith(getChave())
        .build()
        .parseSignedClaims(token)
        .getPayload();

    }

    private SecretKey getChave(){
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

}
