package br.com.easybiz.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.easybiz.dto.AvaliacaoDTO;
import br.com.easybiz.model.Avaliacao;
import br.com.easybiz.service.AvaliacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/avaliacoes")
@Tag(name = "Avaliações", description = "Notas e reviews de serviços")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping("/pedido/{pedidoId}")
    @Operation(summary = "Avaliar um serviço", description = "Requer que o pedido esteja CONCLUIDO")
    public ResponseEntity<Avaliacao> avaliar(
            @PathVariable Long pedidoId,
            @RequestBody AvaliacaoDTO dto,
            // Aqui pegamos o ID do token automaticamente (Segurança JWT)
            @AuthenticationPrincipal Long usuarioLogadoId
    ) {
        // Se o @AuthenticationPrincipal vier nulo (depende da sua config de security),
        // você pode pegar do SecurityContextHolder como fizemos antes.
        // Mas assumindo que seu filtro passa o ID como Principal:
        return ResponseEntity.ok(avaliacaoService.avaliarPedido(pedidoId, usuarioLogadoId, dto));
    }
}