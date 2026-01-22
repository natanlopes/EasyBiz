package br.com.easybiz.controller;

import br.com.easybiz.dto.CriarPedidoServicoDTO;
import br.com.easybiz.model.PedidoServico;
import br.com.easybiz.service.PedidoServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Tag(name = "Pedidos de Serviço", description = "Pedidos iniciados por clientes")
@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoServicoController {

    private final PedidoServicoService pedidoServicoService;
    
    public PedidoServicoController(PedidoServicoService pedidoServicoService) {
        this.pedidoServicoService = pedidoServicoService;
    }
    @Operation(
            summary = "Criar pedido de serviço",
            description = """
                Cliente inicia uma conversa com um negócio para negociar um serviço.
                
                Regras:
                - Cliente deve existir
                - Negócio deve existir
                - Pedido inicia com status ABERTO
                """
        )
    @PostMapping("/cliente/{clienteId}")
    public ResponseEntity<PedidoServico> criarPedido(
            @PathVariable Long clienteId,
            @RequestBody CriarPedidoServicoDTO dto
    ) {
        return ResponseEntity.ok(
                pedidoServicoService.criarPedido(clienteId, dto)
        );
    }
}

