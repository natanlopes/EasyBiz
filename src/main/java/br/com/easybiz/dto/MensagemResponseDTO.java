package br.com.easybiz.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta contendo os dados de uma mensagem do chat")
public record MensagemResponseDTO(

        @Schema(description = "ID da mensagem", example = "1")
        Long id,

        @Schema(description = "ID do pedido de serviço", example = "10")
        Long pedidoServicoId,

        @Schema(description = "ID do usuário remetente", example = "5")
        Long remetenteId,

        @Schema(description = "Nome do remetente", example = "João Silva")
        String remetenteNome,

        @Schema(description = "Conteúdo da mensagem", example = "Olá, gostaria de saber o status do pedido")
        String conteudo,

        @Schema(description = "Data e hora do envio da mensagem")
        LocalDateTime enviadoEm,
        @Schema(description = "Mensagem lida")
        Boolean lida,
        @Schema(description = "Mensagem lida e data e horario")
        LocalDateTime lidaEm
) {
	
	
}
