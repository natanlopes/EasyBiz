package br.com.easybiz.controller;

import br.com.easybiz.dto.CriarNegocioDTO;
import br.com.easybiz.model.Negocio;
import br.com.easybiz.service.NegocioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Tag(name = "Negócios", description = "Gerenciamento de negócios cadastrados na plataforma")
@RestController
@RequestMapping("/negocios")
@RequiredArgsConstructor

public class NegocioController {

    private final NegocioService negocioService;
    
    public NegocioController(NegocioService negocioService) {
        this.negocioService = negocioService;
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
    public ResponseEntity<Negocio> criar(@RequestBody @Valid CriarNegocioDTO dto) {
        Negocio negocio = negocioService.criarNegocio(
                dto.usuarioId(),
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
}

