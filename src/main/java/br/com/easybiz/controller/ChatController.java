package br.com.easybiz.controller;

import br.com.easybiz.dto.DigitandoDTO;
import br.com.easybiz.dto.EnviarMensagemDTO;
import br.com.easybiz.dto.MensagemLidaDTO;
import br.com.easybiz.dto.MensagemResponseDTO;
import br.com.easybiz.service.MensagemService;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final MensagemService mensagemService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(
            MensagemService mensagemService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.mensagemService = mensagemService;
        this.messagingTemplate = messagingTemplate;
    }

    // 🔹 1. ENVIO DE MENSAGEM (JWT manda no remetente)
    @MessageMapping("/chat/{pedidoId}")
    public void enviarMensagemEmTempoReal(
            @DestinationVariable Long pedidoId,
            EnviarMensagemDTO dto,
            Principal principal
    ) {

        // 🔐 ID REAL vem do JWT
        Long remetenteId = Long.valueOf(principal.getName());

        MensagemResponseDTO mensagem = mensagemService.enviarMensagem(
                pedidoId,
                remetenteId,
                dto.conteudo()
        );

        // 📡 envia para o tópico
        messagingTemplate.convertAndSend(
                "/topic/mensagens/" + pedidoId,
                mensagem
        );
    }

    // 🔹 2. DIGITANDO (não persiste)
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

    // 🔹 3. MENSAGEM LIDA
    @MessageMapping("/chat/{pedidoId}/lida/{mensagemId}")
    public void confirmarLeitura(
            @DestinationVariable Long pedidoId,
            @DestinationVariable Long mensagemId,
            Principal principal
    ) {
        Long quemLeuId = Long.valueOf(principal.getName());

        // salva leitura
        mensagemService.marcarMensagemEspecifica(
                pedidoId,
                mensagemId,
                quemLeuId
        );

        // avisa "lida"
        messagingTemplate.convertAndSend(
                "/topic/mensagens/" + pedidoId + "/lida",
                new MensagemLidaDTO(
                        mensagemId,
                        pedidoId,
                        quemLeuId,
                        LocalDateTime.now()
                )
        );

        // último visto
        var ultimoVisto = mensagemService.buscarUltimoVisto(
                pedidoId,
                quemLeuId
        );

        messagingTemplate.convertAndSend(
                "/topic/mensagens/" + pedidoId + "/ultimo-visto",
                ultimoVisto
        );
    }
}
