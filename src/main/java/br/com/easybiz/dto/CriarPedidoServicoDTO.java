package br.com.easybiz.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarPedidoServicoDTO(
    @Schema(description = "ID do negocio prestador", example = "1")
    @NotNull(message = "O ID do negocio e obrigatorio")
    Long negocioId,

    @Schema(description = "Descricao do servico solicitado", example = "Corte de cabelo e barba")
    @NotBlank(message = "A descricao e obrigatoria")
    @Size(max=500)
    String descricao,

    @Schema(description = "Data desejada (ISO 8601)", example = "2026-02-10T15:30:00")
    @NotNull(message = "A data desejada e obrigatoria")
    @Future(message = "A data deve ser futura")
    LocalDateTime dataDesejada
) {}
