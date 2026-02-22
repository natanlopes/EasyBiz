package br.com.easybiz.controller;

import java.security.Principal;

import br.com.easybiz.dto.UsuarioPerfilPublicoDTO;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.easybiz.dto.AtualizarFotoDTO;
import br.com.easybiz.dto.CriarUsuarioDTO;
import br.com.easybiz.dto.UsuarioResponseDTO;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.UsuarioRepository;
import br.com.easybiz.service.AuthContextService;
import br.com.easybiz.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "1. Usuários", description = "Gestão de contas de usuários")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;
    private final UsuarioRepository usuarioRepository;
    private final AuthContextService authContextService;

    public UsuarioController(UsuarioService service, UsuarioRepository usuarioRepository, AuthContextService authContextService) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
        this.authContextService = authContextService;
    }

    @Operation(summary = "Cadastrar Usuário", description = "Cria uma conta nova.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@RequestBody @Valid CriarUsuarioDTO dto) {
        Usuario novoUsuario = service.criarUsuario(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioResponseDTO(
                novoUsuario.getId(),
                novoUsuario.getNomeCompleto(),
                novoUsuario.getEmail(),
                novoUsuario.getFotoUrl()
        ));
    }


    @Operation(summary = "Meus Dados", description = "Retorna dados do usuário logado baseados no Token.")
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UsuarioResponseDTO> meusDados(Principal principal) {
        // Resolve ID via AuthContextService
        Long meuId = authContextService.getUsuarioIdByEmail(principal.getName());

        return usuarioRepository.findById(meuId)
                .map(u -> ResponseEntity.ok(new UsuarioResponseDTO(
                        u.getId(),
                        u.getNomeCompleto(),
                        u.getEmail(),
                        u.getFotoUrl()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Perfil Público", description = "Busca nome e foto de outro usuário pelo ID.")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioPerfilPublicoDTO> buscarPorId(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(u -> ResponseEntity.ok(new UsuarioPerfilPublicoDTO(
                        u.getId(),
                        u.getNomeCompleto(),
                        u.getFotoUrl()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Atualizar minha foto", description = "Define a URL da foto de perfil.")
    @PatchMapping("/me/foto")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> atualizarMinhaFoto(
            @RequestBody @Valid AtualizarFotoDTO dto,
            Principal principal
    ) {
        Long usuarioLogadoId = authContextService.getUsuarioIdByEmail(principal.getName());
        service.atualizarFoto(usuarioLogadoId, dto.url());
        return ResponseEntity.noContent().build();
    }
}