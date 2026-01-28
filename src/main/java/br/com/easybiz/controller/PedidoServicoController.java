package br.com.easybiz.controller;

import br.com.easybiz.dto.CriarPedidoServicoDTO;
import br.com.easybiz.model.PedidoServico;
import br.com.easybiz.service.PedidoServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

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

	@Operation(summary = "Criar pedido de serviço", description = """
			Cliente inicia uma conversa com um negócio para negociar um serviço.

			Regras:
			- Cliente deve existir
			- Negócio deve existir
			- Pedido inicia com status ABERTO
			""")
	@PostMapping("/cliente/{clienteId}")
		public ResponseEntity<PedidoServico> criarPedido(@PathVariable Long clienteId,
				@RequestBody CriarPedidoServicoDTO dto) {
			return ResponseEntity.ok(pedidoServicoService.criarPedido(clienteId, dto));
		}
    @Operation(summary = "Aceitar Pedido", description = "Prestador aceita o serviço (Muda status para ACEITO)")
    @PatchMapping("/{id}/aceitar")
    public ResponseEntity<PedidoServico> aceitarPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoServicoService.aceitarPedido(id));
    }

    @Operation(summary = "Recusar Pedido", description = "Prestador recusa o serviço (Muda status para RECUSADO)")
    @PatchMapping("/{id}/recusar")
    public ResponseEntity<PedidoServico> recusarPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoServicoService.recusarPedido(id));
    }

    @Operation(summary = "Concluir Serviço", description = "Finaliza o trabalho (Muda status para CONCLUIDO)")
    @PatchMapping("/{id}/concluir")
    public ResponseEntity<PedidoServico> concluirPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoServicoService.concluirPedido(id));
    }
    
    @Operation(summary = "Listar pedidos do Cliente", description = "Histórico de compras do usuário")
    @GetMapping("/cliente/{clienteId}") // <--- A URL que você perguntou!
    public ResponseEntity<List<PedidoServico>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(pedidoServicoService.listarPorCliente(clienteId));
    }

    @Operation(summary = "Listar pedidos do Prestador", description = "Agenda de serviços do dono do negócio")
    @GetMapping("/prestador/{prestadorId}")
    public ResponseEntity<List<PedidoServico>> listarPorPrestador(@PathVariable Long prestadorId) {
        return ResponseEntity.ok(pedidoServicoService.listarPorPrestador(prestadorId));
    }
}

