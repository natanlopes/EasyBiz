package br.com.easybiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para redefinir a senha com o codigo recebido por e-mail")
public record RedefinirSenhaRequestDTO(
    @Schema(description = "Codigo de 6 digitos recebido por e-mail", example = "847291")
    @NotBlank(message = "O codigo e obrigatorio")
    String token,

    @Schema(description = "Nova senha do usuario", example = "novaSenha123")
    @NotBlank(message = "A nova senha e obrigatoria")
    @Size(min = 6, message = "A senha deve ter no minimo 6 caracteres")
    String novaSenha
) {}
