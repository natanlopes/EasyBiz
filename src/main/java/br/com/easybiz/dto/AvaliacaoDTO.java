package br.com.easybiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados necessarios para avaliacao")
public record AvaliacaoDTO(
        @Schema(example = "5", description = "Nota da avaliacao (1 a 5)")
        @NotNull(message = "A nota e obrigatoria")
        @Min(value = 1, message = "A nota minima e 1")
        @Max(value = 5, message = "A nota maxima e 5")
        Integer nota,

        @Schema(example = "Excelente servico!", description = "Comentario sobre o servico")
        @Size(max = 500, message = "O comentario pode ter no maximo 500 caracteres")
        String comentario
) {}
