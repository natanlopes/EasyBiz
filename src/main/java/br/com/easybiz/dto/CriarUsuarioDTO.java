package br.com.easybiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CriarUsuarioDTO(
        @Schema(description = "Nome completo do proprietario", example = "Carlos Silva")
        @NotBlank
        @Pattern(regexp = "^[^<>]*$", message = "Caracteres HTML não são permitidos por segurança")
        String nomeCompleto,

        @Schema(description = "E-mail para login", example = "carlos@easybiz.com")
        @NotBlank @Email
        String email,

        @Schema(description = "Senha de acesso", example = "123456")
        @NotBlank
        String senha
) {}
