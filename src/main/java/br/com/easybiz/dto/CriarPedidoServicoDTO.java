package br.com.easybiz.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record CriarPedidoServicoDTO(
	@Schema(description = "ID do negócio prestador", example = "1")
    Long negocioId,
    @Schema(description = "Descrição do serviço solicitado", example = "Corte de cabelo e barba")
    String descricao,
    @Schema(description = "Data desejada (ISO 8601)", example = "2026-02-10T15:30:00")
    LocalDateTime dataDesejada
) {}
