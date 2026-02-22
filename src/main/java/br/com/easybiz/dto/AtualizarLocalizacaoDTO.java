package br.com.easybiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para atualizar a localizacao de um negocio")
public record AtualizarLocalizacaoDTO(
        @Schema(example = "-23.5505", description = "Latitude do negocio")
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,

        @Schema(example = "-46.6333", description = "Longitude do negocio")
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,

        @Schema(example = "Rua Example, 123 - Sao Paulo, SP", description = "Endereco completo do negocio")
        String enderecoCompleto
) {}