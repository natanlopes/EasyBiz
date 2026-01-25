package br.com.easybiz.security;

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
public class JwtService {

    // Pegando do application.properties
    @Value("${api.security.token.secret}")
    private String secretKey;

    @Value("${api.security.token.expiration}")
    private long jwtExpiration;

    // 🟢 1. Extrair Username (que no nosso caso será o ID em String)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 🟢 2. Extrair ID convertido para Long
    public Long extractUserId(String token) {
        String subject = extractUsername(token);
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Token inválido: Subject não é um ID numérico");
        }
    }

    // 🟢 3. Gerar Token (IMPORTANTE: Salvando ID no Subject)
    public String generateToken(Long usuarioId) {
        return Jwts.builder()
                .setSubject(String.valueOf(usuarioId)) // <--- AQUI O SEGREDO
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 🟢 4. Validar Token
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}