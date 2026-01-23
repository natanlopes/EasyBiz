package br.com.easybiz.controller;

import br.com.easybiz.dto.EnviarMensagemDTO;
import br.com.easybiz.dto.MensagemResponseDTO;
import br.com.easybiz.service.MensagemService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

	private final MensagemService mensagemService;

	public ChatController(MensagemService mensagemService) {
		this.mensagemService = mensagemService;
	}

	@MessageMapping("/chat/{pedidoId}")
	@SendTo("/topic/mensagens/{pedidoId}")
	public MensagemResponseDTO enviarMensagemEmTempoReal(@DestinationVariable Long pedidoId, EnviarMensagemDTO dto) {
		// Agora retorna o DTO (com ID, data formatada, nome do remetente, etc)
		return mensagemService.enviarMensagem(pedidoId, dto);
	}
}
