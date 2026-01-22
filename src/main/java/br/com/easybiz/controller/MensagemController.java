package br.com.easybiz.controller;

import br.com.easybiz.dto.EnviarMensagemDTO;
import br.com.easybiz.model.Mensagem;
import br.com.easybiz.service.MensagemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "5. Chat do Pedido", description = "Envio e leitura de mensagens entre cliente e prestador")
@RestController
@RequestMapping("/pedidos/{pedidoId}/mensagens")
public class MensagemController {

    private final MensagemService mensagemService;

    public MensagemController(MensagemService mensagemService) {
        this.mensagemService = mensagemService;
    }

    @Operation(summary = "Enviar mensagem", description = "Registra uma nova mensagem no chat do pedido.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mensagem enviada"),
        @ApiResponse(responseCode = "404", description = "Pedido ou Usuário não encontrado")
    })
    @PostMapping
    public ResponseEntity<Mensagem> enviar(
            @PathVariable Long pedidoId,
            @RequestBody @Valid EnviarMensagemDTO dto
    ) {
        return ResponseEntity.ok(mensagemService.enviarMensagem(pedidoId, dto));
    }

    @Operation(summary = "Histórico de conversa", description = "Lista todas as mensagens do pedido em ordem cronológica.")
    @GetMapping
    public ResponseEntity<List<Mensagem>> listar(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(mensagemService.listarMensagens(pedidoId));
    }
}