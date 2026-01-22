package br.com.easybiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarNegocioDTO(
        @NotNull Long usuarioId,
        @NotBlank String nome,
        @NotBlank String tipo
        
) {

	public Long usuarioId() {
		return usuarioId;
	}

	public String nome() {
		return nome;
	}

	public String tipo() {
		return tipo;
	}
}

