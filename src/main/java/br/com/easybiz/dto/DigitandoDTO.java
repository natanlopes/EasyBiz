package br.com.easybiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Usuario digitando")
public record DigitandoDTO(
		@Schema(description = "ID do usuario", example = "1")
        Long usuarioId,
        @Schema(description = "Nome do usuario", example = "João Silva")
        String usuarioNome,
        @Schema(description = "Digitando", example = "João Silva digitando")
        Boolean digitando
) {
}
