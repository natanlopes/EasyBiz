package br.com.easybiz.controller;

import br.com.easybiz.dto.EsqueciSenhaRequestDTO;
import br.com.easybiz.dto.LoginRequestDTO;
import br.com.easybiz.dto.LoginResponseDTO;
import br.com.easybiz.dto.RedefinirSenhaRequestDTO;
import br.com.easybiz.exception.UnauthorizedException;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.UsuarioRepository;
import br.com.easybiz.security.JwtService;
import br.com.easybiz.service.PasswordResetService;
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

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticacao", description = "Login para pegar o Token JWT")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetService passwordResetService;

    public AuthController(
        UsuarioRepository usuarioRepository,
        JwtService jwtService,
        PasswordEncoder passwordEncoder,
        PasswordResetService passwordResetService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetService = passwordResetService;
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

    @PostMapping("/esqueci-senha")
    @Operation(
        summary = "Solicitar recuperacao de senha",
        description = "Envia um codigo de 6 digitos para o e-mail cadastrado. Sempre retorna 200 por seguranca.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Se o e-mail existir, um codigo sera enviado")
        }
    )
    public ResponseEntity<Map<String, String>> esqueciSenha(
            @RequestBody @Valid EsqueciSenhaRequestDTO request
    ) {
        passwordResetService.solicitarReset(request.email());
        return ResponseEntity.ok(Map.of(
            "mensagem", "Se o e-mail estiver cadastrado, voce recebera um codigo de recuperacao"
        ));
    }

    @PostMapping("/redefinir-senha")
    @Operation(
        summary = "Redefinir senha com codigo",
        description = "Recebe o codigo de 6 digitos e a nova senha para redefinir",
        responses = {
            @ApiResponse(responseCode = "200", description = "Senha redefinida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Codigo invalido, expirado ou ja utilizado")
        }
    )
    public ResponseEntity<Map<String, String>> redefinirSenha(
            @RequestBody @Valid RedefinirSenhaRequestDTO request
    ) {
        passwordResetService.redefinirSenha(request.token(), request.novaSenha());
        return ResponseEntity.ok(Map.of(
            "mensagem", "Senha redefinida com sucesso"
        ));
    }
}
