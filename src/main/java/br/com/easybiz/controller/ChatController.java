package br.com.easybiz.controller;

import br.com.easybiz.dto.DigitandoDTO;
import br.com.easybiz.dto.EnviarMensagemDTO;
import br.com.easybiz.dto.MensagemLidaDTO;
import br.com.easybiz.dto.MensagemResponseDTO;
import br.com.easybiz.service.MensagemService;

import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate; // <--- Importante!
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final MensagemService mensagemService;
    private final SimpMessagingTemplate messagingTemplate; // <--- Declarando a variável que faltava

    // Construtor atualizado recebendo as duas dependências
    public ChatController(MensagemService mensagemService, SimpMessagingTemplate messagingTemplate) {
        this.mensagemService = mensagemService;
        this.messagingTemplate = messagingTemplate;
    }

    // 🔹 1. ENVIO DE MENSAGEM (Persistência + WebSocket)
    // Usa @SendTo, então o retorno do método é enviado automaticamente para o tópico
    @MessageMapping("/chat/{pedidoId}")
    @SendTo("/topic/mensagens/{pedidoId}")
    public MensagemResponseDTO enviarMensagemEmTempoReal(
            @DestinationVariable Long pedidoId, 
            EnviarMensagemDTO dto
    ) {
        return mensagemService.enviarMensagem(pedidoId, dto);
    }

    // 🔹 2. INDICADOR "DIGITANDO..." (Apenas WebSocket)
    // Não salva no banco. Usa o messagingTemplate para enviar manualmente.
    @MessageMapping("/chat/{pedidoId}/digitando")
    public void digitando(
            @DestinationVariable Long pedidoId,
            DigitandoDTO dto
    ) {
        messagingTemplate.convertAndSend(
                "/topic/mensagens/" + pedidoId + "/digitando",
                dto
        );
    }

    @MessageMapping("/chat/{pedidoId}/lida/{mensagemId}")
    public void confirmarLeituraEspecifica(
            @DestinationVariable Long pedidoId,
            @DestinationVariable Long mensagemId,
            DigitandoDTO dto // dto.usuarioId() é QUEM LEU
    ) {
        // 1. Persiste a leitura no banco (já estava feito)
        mensagemService.marcarMensagemEspecifica(pedidoId, mensagemId, dto.usuarioId());

        // 2. Avisa que as mensagens ficaram azuis (✅) (já estava feito)
        messagingTemplate.convertAndSend(
            "/topic/mensagens/" + pedidoId + "/lida",
            new MensagemLidaDTO(mensagemId, pedidoId, dto.usuarioId(), LocalDateTime.now())
        );

        // -----------------------------------------------------------
        // 3. NOVO: Calcula e avisa o "Visto por último" 
        // -----------------------------------------------------------
        var ultimoVisto = mensagemService.buscarUltimoVisto(pedidoId, dto.usuarioId());
        
        // Dispara para o tópico específico de status
        messagingTemplate.convertAndSend(
            "/topic/mensagens/" + pedidoId + "/ultimo-visto",
            ultimoVisto
        );
    }


}