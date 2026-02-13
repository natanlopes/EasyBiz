package br.com.easybiz.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarPedidoServicoDTO(
    @Schema(description = "ID do negocio prestador", example = "1")
    @NotNull(message = "O ID do negocio e obrigatorio")
    Long negocioId,

    @Schema(description = "Descricao do servico solicitado", example = "Corte de cabelo e barba")
    @NotBlank(message = "A descricao e obrigatoria")
    String descricao,

    @Schema(description = "Data desejada (ISO 8601)", example = "2026-02-10T15:30:00")
    LocalDateTime dataDesejada
) {}
