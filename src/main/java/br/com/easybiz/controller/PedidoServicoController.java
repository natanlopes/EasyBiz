package br.com.easybiz.controller;

import br.com.easybiz.dto.CriarPedidoServicoDTO;
import br.com.easybiz.model.PedidoServico;
import br.com.easybiz.service.PedidoServicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoServicoController {

    private final PedidoServicoService pedidoServicoService;
    
    public PedidoServicoController(PedidoServicoService pedidoServicoService) {
        this.pedidoServicoService = pedidoServicoService;
    }
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

