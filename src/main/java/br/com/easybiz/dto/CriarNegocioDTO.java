package br.com.easybiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
@Schema(description = "Dados necessários para criação de um negócio")
public record CriarNegocioDTO(
		@Schema(example = "1", description = "ID do usuário dono do negócio")
        @NotNull Long usuarioId,
        @Schema(example = "EasyBiz Barbearia", description = "Nome do negócio")
        @NotBlank String nome,
        @Schema(example = "BARBEARIA", description = "Categoria do serviço (Ex: Pedreiro, Manicure, Python Dev)")
        @NotBlank
        String categoria

) {

	public Long usuarioId() {
		return usuarioId;
	}

	public String nome() {
		return nome;
	}

	public String tipo() {
		return categoria;
	}
}

