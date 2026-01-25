package br.com.easybiz.security;

import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository; // 1. Adicionamos o Repo

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            // 2. Ajuste de nome: tokenValido -> isTokenValid
            if (jwtService.isTokenValid(token)) {
                
                // 3. Ajuste de nome: extrairUsuarioId -> extractUserId
                Long usuarioId = jwtService.extractUserId(token);

                // 4. Buscamos o usuário real para colocar no Contexto
                // Isso permite usar @AuthenticationPrincipal Usuario u nos Controllers REST
                Usuario usuario = usuarioRepository.findById(usuarioId)
                        .orElse(null);

                if (usuario != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    usuario, // Passamos o OBJETO, não só o ID
                                    null,
                                    Collections.emptyList()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}