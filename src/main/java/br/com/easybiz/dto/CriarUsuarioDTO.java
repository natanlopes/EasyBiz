package br.com.easybiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CriarUsuarioDTO(
		@Schema(description = "Nome completo do proprietário", example = "Carlos Silva")
        @NotBlank
        String nomeCompleto,

        @Schema(description = "E-mail para login", example = "carlos@easybiz.com")
        @NotBlank @Email
        String email,

        @Schema(description = "Senha de acesso", example = "123456")
        @NotBlank
        String senha
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
