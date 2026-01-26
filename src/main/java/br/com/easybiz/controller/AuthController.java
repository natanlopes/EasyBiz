package br.com.easybiz.controller;

import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.UsuarioRepository;
import br.com.easybiz.security.JwtService;
import io.swagger.v3.oas.annotations.Operation; 
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Login para pegar o Token JWT")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository usuarioRepository, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Realizar Login", description = "Recebe email/senha e retorna o Token JWT")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // 1. Busca usuário
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 2. Valida senha
        if (!usuario.getSenha().equals(request.senha())) {
            return ResponseEntity.badRequest().body("Senha inválida");
        }

        // 3. Gera Token com ID
        String token = jwtService.gerarToken(usuario.getId());

        return ResponseEntity.ok(Map.of("token", token));
    }
}

// DTO interno
record LoginRequest(String email, String senha) {}