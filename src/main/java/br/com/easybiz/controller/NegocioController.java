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
import br.com.easybiz.dto.NegocioResponseDTO;
import br.com.easybiz.model.Negocio;
import br.com.easybiz.service.AuthContextService;
import br.com.easybiz.service.NegocioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Negócios", description = "Gerenciamento de negócios cadastrados na plataforma")
@RestController
@RequestMapping("/negocios")
public class NegocioController {

    private final NegocioService negocioService;
    private final AuthContextService authContextService;

    public NegocioController(NegocioService negocioService, AuthContextService authContextService) {
        this.negocioService = negocioService;
        this.authContextService = authContextService;
    }

    @Operation(
            summary = "Criar um novo negócio",
            description = """
                Cria um negócio vinculado ao usuário autenticado.

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
    public ResponseEntity<NegocioResponseDTO> criar(@RequestBody @Valid CriarNegocioDTO dto, Principal principal) {
        Long usuarioLogadoId = authContextService.getUsuarioIdByEmail(principal.getName());

        Negocio negocio = negocioService.criarNegocio(
                usuarioLogadoId,
                dto.nome(),
                dto.categoria()
        );
        return ResponseEntity.ok(NegocioResponseDTO.fromEntity(negocio));
    }

    @GetMapping("/busca")
    @Operation(summary = "Busca inteligente por localização e ranking")
    public ResponseEntity<List<NegocioResponseDTO>> buscar(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(required = false) String busca
    ) {
        List<NegocioResponseDTO> resultado = negocioService.buscarNegocios(lat, lon, busca)
                .stream()
                .map(NegocioResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(resultado);
    }

    @PatchMapping("/{id}/logo")
    @Operation(summary = "Atualizar Logo do Negócio", description = "Requer que o usuário logado seja o dono.")
    public ResponseEntity<Void> atualizarLogo(
            @PathVariable Long id,
            @RequestBody @Valid AtualizarFotoDTO dto,
            Principal principal
    ) {
        Long usuarioLogadoId = authContextService.getUsuarioIdByEmail(principal.getName());

        negocioService.atualizarLogo(id, usuarioLogadoId, dto.url());

        return ResponseEntity.noContent().build();
    }
}