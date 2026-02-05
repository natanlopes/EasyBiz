package br.com.easybiz.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Objeto de confirmação quando uma mensagem é visualizada")
public record MensagemLidaDTO(

    @Schema(description = "ID da mensagem que foi lida", example = "150")
    Long mensagemId,

    @Schema(description = "ID do pedido de serviço", example = "10")
    Long pedidoId,

    @Schema(description = "ID do usuário que visualizou a mensagem", example = "5")
    Long quemLeuId,

    @Schema(description = "Data e hora exata da leitura")
    LocalDateTime lidaEm
) {}