package br.com.easybiz.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public UsuarioController(UsuarioService service, UsuarioRepository usuarioRepository) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    // ==========================================
    // 1. CADASTRAR (PÚBLICO)
    // ==========================================
    @Operation(summary = "Cadastrar Usuário", description = "Cria uma conta nova.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuário criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@RequestBody @Valid CriarUsuarioDTO dto) {
        Usuario novoUsuario = service.criarUsuario(dto);

        // Retorna DTO para não expor a senha!
        return ResponseEntity.ok(new UsuarioResponseDTO(
                novoUsuario.getId(),
                novoUsuario.getNomeCompleto(),
                novoUsuario.getEmail(),
                novoUsuario.getFotoUrl()
        ));
    }

    // ==========================================
    // 2. MEUS DADOS (PRIVADO - USADO NA HOME DO APP)
    // ==========================================
    @Operation(summary = "Meus Dados", description = "Retorna dados do usuário logado baseados no Token.")
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> meusDados() {
        // Pega o ID de dentro do Token JWT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long meuId = Long.valueOf(auth.getName());

        return usuarioRepository.findById(meuId)
                .map(u -> ResponseEntity.ok(new UsuarioResponseDTO(
                        u.getId(),
                        u.getNomeCompleto(),
                        u.getEmail(),
                        u.getFotoUrl()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // 3. PERFIL PÚBLICO (USADO NO CHAT)
    // ==========================================
    @Operation(summary = "Perfil Público", description = "Busca nome e foto de outro usuário pelo ID.")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(u -> ResponseEntity.ok(new UsuarioResponseDTO(
                        u.getId(),
                        u.getNomeCompleto(),
                        u.getEmail(),
                        u.getFotoUrl()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // 4. ATUALIZAR FOTO (PRIVADO)
    // ==========================================
    @Operation(summary = "Atualizar minha foto", description = "Define a URL da foto de perfil.")
    @PatchMapping("/me/foto")
    public ResponseEntity<Void> atualizarMinhaFoto(
            @RequestBody @Valid AtualizarFotoDTO dto,
            Principal principal
    ) {
        Long usuarioLogadoId = Long.valueOf(principal.getName());

        Usuario usuario = usuarioRepository.findById(usuarioLogadoId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setFotoUrl(dto.url());
        usuarioRepository.save(usuario);

        return ResponseEntity.noContent().build();
    }
}