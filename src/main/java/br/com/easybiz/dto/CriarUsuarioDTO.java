package br.com.easybiz.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CriarUsuarioDTO(
        @NotBlank String nomeCompleto,
        @NotBlank @Email String email,
        @NotBlank String senha
) {

	public String nomeCompleto() {
		return nomeCompleto;
	}

	public String email() {
		return email;
	}

	public String senha() {
		return senha;
	}
	
}
