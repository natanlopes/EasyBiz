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
import br.com.easybiz.service.AuthContextService;
import br.com.easybiz.service.MensagemService;

@Controller
public class ChatController {

    private final MensagemService mensagemService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuthContextService authContextService;

    public ChatController(
            MensagemService mensagemService,
            SimpMessagingTemplate messagingTemplate,
            AuthContextService authContextService
    ) {
        this.mensagemService = mensagemService;
        this.messagingTemplate = messagingTemplate;
        this.authContextService = authContextService;
    }

    // 1. Envio de Mensagem (WebSocket)
    @MessageMapping("/chat/{pedidoId}")
    public void enviarMensagemEmTempoReal(
            @DestinationVariable Long pedidoId,
            EnviarMensagemDTO dto,
            Principal principal
    ) {
        String emailRemetente = principal.getName();

        // Service aceita Email (String)
        MensagemResponseDTO mensagem = mensagemService.enviarMensagem(
                pedidoId,
                emailRemetente,
                dto.conteudo()
        );

        messagingTemplate.convertAndSend("/topic/mensagens/" + pedidoId, mensagem);
    }

    // 2. Notificação de "Digitando..."
    @MessageMapping("/chat/{pedidoId}/digitando")
    public void digitando(
            @DestinationVariable Long pedidoId,
            DigitandoDTO dto,
            Principal principal
    ) {
        // Precisamos do ID numérico para o DTO do frontend
        Long usuarioId = authContextService.getUsuarioIdByEmail(principal.getName());

        messagingTemplate.convertAndSend(
                "/topic/mensagens/" + pedidoId + "/digitando",
                new DigitandoDTO(usuarioId, dto.usuarioNome(), dto.digitando())
        );
    }

    // 3. Confirmação de Leitura
    @MessageMapping("/chat/{pedidoId}/lida/{mensagemId}")
    public void confirmarLeitura(
            @DestinationVariable Long pedidoId,
            @DestinationVariable Long mensagemId,
            Principal principal
    ) {
        String emailUsuario = principal.getName();
        Long quemLeuId = authContextService.getUsuarioIdByEmail(emailUsuario);

        // Persiste usando Email
        mensagemService.marcarMensagemEspecifica(pedidoId, mensagemId, emailUsuario);

        // Notifica usando ID
        messagingTemplate.convertAndSend(
                "/topic/mensagens/" + pedidoId + "/lida",
                new MensagemLidaDTO(mensagemId, pedidoId, quemLeuId, LocalDateTime.now())
        );

        var ultimoVisto = mensagemService.buscarUltimoVisto(pedidoId, emailUsuario);

        messagingTemplate.convertAndSend(
                "/topic/mensagens/" + pedidoId + "/ultimo-visto",
                ultimoVisto
        );
    }
}