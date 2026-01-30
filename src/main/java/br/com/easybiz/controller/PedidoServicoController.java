package br.com.easybiz.controller;

import br.com.easybiz.dto.CriarPedidoServicoDTO;
import br.com.easybiz.dto.PedidoServicoResponseDTO;
import br.com.easybiz.service.PedidoServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Tag(name = "Pedidos de Serviço")
@RestController
@RequestMapping("/pedidos")
@SecurityRequirement(name = "bearerAuth") // Exige Token no Swagger
public class PedidoServicoController {

    private final PedidoServicoService service;

    public PedidoServicoController(PedidoServicoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PedidoServicoResponseDTO> criar(
            @RequestBody CriarPedidoServicoDTO dto,
            Principal principal
    ) {
        Long clienteId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(service.criar(clienteId, dto));
    }

    @GetMapping
    @Operation(summary = "Meus Pedidos", description = "Lista pedidos onde sou cliente ou prestador")
    public ResponseEntity<List<PedidoServicoResponseDTO>> listarMeusPedidos(Principal principal) {
        Long usuarioId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(service.listarMeusPedidos(usuarioId));
    }

    @PatchMapping("/{id}/aceitar")
    public ResponseEntity<Void> aceitar(@PathVariable Long id, Principal principal) {
        service.aceitar(id, Long.valueOf(principal.getName()));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/recusar")
    public ResponseEntity<Void> recusar(@PathVariable Long id, Principal principal) {
        service.recusar(id, Long.valueOf(principal.getName()));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<Void> concluir(@PathVariable Long id, Principal principal) {
        service.concluir(id, Long.valueOf(principal.getName()));
        return ResponseEntity.noContent().build();
    }
}