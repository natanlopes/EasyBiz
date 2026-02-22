package br.com.easybiz.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.easybiz.dto.CriarPedidoServicoDTO;
import br.com.easybiz.dto.PedidoServicoResponseDTO;
import br.com.easybiz.service.AuthContextService;
import br.com.easybiz.service.PedidoServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Pedidos de Servico")
@RestController
@RequestMapping("/pedidos")
@SecurityRequirement(name = "bearerAuth")
public class PedidoServicoController {

    private final PedidoServicoService service;
    private final AuthContextService authContextService;

    public PedidoServicoController(PedidoServicoService service, AuthContextService authContextService) {
        this.service = service;
        this.authContextService = authContextService;
    }

    @PostMapping
    public ResponseEntity<PedidoServicoResponseDTO> criar(
            @RequestBody @Valid CriarPedidoServicoDTO dto,
            Principal principal
    ) {
        String emailCliente = principal.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(emailCliente, dto));
    }

    @GetMapping
    @Operation(summary = "Meus Pedidos", description = "Lista pedidos onde sou cliente ou prestador")
    public ResponseEntity<Page<PedidoServicoResponseDTO>> listarMeusPedidos(
            Principal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long usuarioId = authContextService.getUsuarioIdByEmail(principal.getName());
        return ResponseEntity.ok(service.listarMeusPedidos(usuarioId, pageable));
    }

    @PatchMapping("/{id}/aceitar")
    public ResponseEntity<Void> aceitar(@PathVariable Long id, Principal principal) {
        service.aceitar(id, authContextService.getUsuarioIdByEmail(principal.getName()));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/recusar")
    public ResponseEntity<Void> recusar(@PathVariable Long id, Principal principal) {
        service.recusar(id, authContextService.getUsuarioIdByEmail(principal.getName()));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<Void> concluir(@PathVariable Long id, Principal principal) {
        service.concluir(id, authContextService.getUsuarioIdByEmail(principal.getName()));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id, Principal principal) {
        service.cancelar(id, authContextService.getUsuarioIdByEmail(principal.getName()));
        return ResponseEntity.noContent().build();
    }
}
