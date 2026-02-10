package br.com.easybiz.controller;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import br.com.easybiz.dto.DigitandoDTO;
import br.com.easybiz.dto.EnviarMensagemDTO;
import br.com.easybiz.dto.MensagemLidaDTO;
import br.com.easybiz.dto.MensagemResponseDTO;
import br.com.easybiz.service.MensagemService;

@Controller
public class ChatController {

    private final MensagemService mensagemService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(MensagemService mensagemService, SimpMessagingTemplate messagingTemplate) {
        this.mensagemService = mensagemService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     *  1. Envio de Mensagem
     * Rota STOMP: /app/chat/{pedidoId}
     * Payload: EnviarMensagemDTO
     */
    @MessageMapping("/chat/{pedidoId}")
    public void enviarMensagemEmTempoReal(
            @DestinationVariable Long pedidoId,
            EnviarMensagemDTO dto,
            Principal principal
    ) {
        // CORREÇÃO: Pegamos o EMAIL direto, sem converter para Long
        String emailRemetente = principal.getName();

        // Passamos o email para o serviço (que agora aceita String)
        MensagemResponseDTO mensagem = mensagemService.enviarMensagem(
                pedidoId,
                emailRemetente,
                dto.conteudo()
        );

        messagingTemplate.convertAndSend(
                "/topic/mensagens/" + pedidoId,
                mensagem
        );
    }

    /**
     * 2. Notificação de "Digitando..."
     * Rota STOMP: /app/chat/{pedidoId}/digitando
     */
    @MessageMapping("/chat/{pedidoId}/digitando")
    public void digitando(
            @DestinationVariable Long pedidoId,
            DigitandoDTO dto,
            Principal principal
    ) {
        Long usuarioId = Long.valueOf(principal.getName());

        messagingTemplate.convertAndSend(
                "/topic/mensagens/" + pedidoId + "/digitando",
                new DigitandoDTO(usuarioId, dto.usuarioNome(), dto.digitando())
        );
    }

    /**
     *  3. Confirmação de Leitura
     * Rota STOMP: /app/chat/{pedidoId}/lida/{mensagemId}
     */
    @MessageMapping("/chat/{pedidoId}/lida/{mensagemId}")
    public void confirmarLeitura(
            @DestinationVariable Long pedidoId,
            @DestinationVariable Long mensagemId,
            Principal principal
    ) {
        Long quemLeuId = Long.valueOf(principal.getName());

        // 1. Persiste no banco que foi lido
        mensagemService.marcarMensagemEspecifica(pedidoId, mensagemId, quemLeuId);

        // 2. Avisa em tempo real: /topic/mensagens/{pedidoId}/lida
        messagingTemplate.convertAndSend(
                "/topic/mensagens/" + pedidoId + "/lida",
                new MensagemLidaDTO(
                        mensagemId,
                        pedidoId,
                        quemLeuId,
                        LocalDateTime.now()
                )
        );

        // 3. Atualiza status de "Visto por último"
        var ultimoVisto = mensagemService.buscarUltimoVisto(pedidoId, quemLeuId);

        messagingTemplate.convertAndSend(
                "/topic/mensagens/" + pedidoId + "/ultimo-visto",
                ultimoVisto
        );
    }
}