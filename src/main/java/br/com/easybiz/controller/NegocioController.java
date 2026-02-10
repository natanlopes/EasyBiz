package br.com.easybiz.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.easybiz.dto.AtualizarFotoDTO;
import br.com.easybiz.dto.CriarNegocioDTO;
import br.com.easybiz.model.Negocio;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.UsuarioRepository; // Importante
import br.com.easybiz.service.NegocioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
@Tag(name = "Negócios", description = "Gerenciamento de negócios cadastrados na plataforma")
@RestController
@RequestMapping("/negocios")


public class NegocioController {

    private final NegocioService negocioService;
    private final UsuarioRepository usuarioRepository; // 🔹 Injeção Necessária

    public NegocioController(NegocioService negocioService, UsuarioRepository usuarioRepository) {
        this.negocioService = negocioService;
        this.usuarioRepository = usuarioRepository;
    }
    @Operation(
            summary = "Criar um novo negócio",
            description = """
                Cria um negócio vinculado a um usuário existente.

                Regras:
                - O usuário deve existir
                - O negócio inicia como ativo
                - Cada usuário pode ter múltiplos negócios
                """
        )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Negócio criado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })

    @PostMapping
    public ResponseEntity<Negocio> criar(
            @RequestBody @Valid CriarNegocioDTO dto,
            Principal principal
    ) {
        // ✅ MELHORIA DE SEGURANÇA:
        // Ignoramos o dto.usuarioId() e usamos o ID do usuário LOGADO (Dono do Token)
        Long usuarioId = recuperarIdUsuario(principal);

        Negocio negocio = negocioService.criarNegocio(
                usuarioId,
                dto.nome(),
                dto.categoria()
        );
        return ResponseEntity.ok(negocio);
    }

    @GetMapping("/busca")
    @Operation(summary = "Busca inteligente por localização e ranking")
    public ResponseEntity<List<Negocio>> buscar(
        @RequestParam Double lat,
        @RequestParam Double lon,
        @RequestParam(required = false) String busca
    ) {
        return ResponseEntity.ok(
            negocioService.buscarNegocios(lat, lon, busca)
        );
    }
    @PatchMapping("/{id}/logo")
    @Operation(summary = "Atualizar Logo do Negócio", description = "Requer que o usuário logado seja o dono.")
    public ResponseEntity<Void> atualizarLogo(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarFotoDTO dto,
            Principal principal
    ) {
        // ✅ CORREÇÃO: Converte Email -> ID antes de chamar o serviço
        Long usuarioLogadoId = recuperarIdUsuario(principal);

        negocioService.atualizarLogo(id, usuarioLogadoId, dto.url());

        return ResponseEntity.noContent().build();
    }
    // =======================================================
    // 🛠️ MÉTODO AUXILIAR
    // =======================================================
    private Long recuperarIdUsuario(Principal principal) {
        String email = principal.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário do token não encontrado no banco."));
        return usuario.getId();
    }
}

