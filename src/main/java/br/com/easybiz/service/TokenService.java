package br.com.easybiz.service;

import br.com.easybiz.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.security.token.expiration}")
    private long expiration;

    // 1. Gerar Token (Usado no Login)
    public String generateToken(Usuario usuario) {
        return Jwts.builder()
                .setSubject(usuario.getEmail()) // Guardamos o Email no token
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. Extrair Email do Token (Usado no WebSocket)
    public String getEmailFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 3. Validar Token
    public boolean isTokenValid(String token, Usuario usuario) {
        final String email = getEmailFromToken(token);
        return (email.equals(usuario.getEmail()) && !isTokenExpired(token));
    }

    // --- Métodos Auxiliares ---

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret); // Se der erro aqui, use getBytes() direto se a senha for simples
        // Se sua senha for texto simples no properties, use: 
        // return Keys.hmacShaKeyFor(secret.getBytes());
        return Keys.hmacShaKeyFor(secret.getBytes()); 
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
