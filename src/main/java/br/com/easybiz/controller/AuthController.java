package br.com.easybiz.controller;

import br.com.easybiz.dto.LoginRequestDTO;
import br.com.easybiz.dto.LoginResponseDTO;
import br.com.easybiz.exception.UnauthorizedException;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.UsuarioRepository;
import br.com.easybiz.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticacao", description = "Login para pegar o Token JWT")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
        UsuarioRepository usuarioRepository,
        JwtService jwtService,
        PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    @Operation(
        summary = "Realizar Login",
        description = "Recebe email/senha e retorna o Token JWT",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Login realizado com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginResponseDTO.class)
                )
            ),
            @ApiResponse(responseCode = "401", description = "Credenciais invalidas")
        }
    )
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody @Valid LoginRequestDTO request
    ) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException("Credenciais invalidas"));

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new UnauthorizedException("Credenciais invalidas");
        }

        String token = jwtService.gerarToken(usuario.getEmail());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}
