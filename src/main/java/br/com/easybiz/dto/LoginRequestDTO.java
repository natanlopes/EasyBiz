package br.com.easybiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para login")
public record LoginRequestDTO(
    @Schema(description = "E-mail do usuario", example = "admin@email.com")
    @NotBlank(message = "O email e obrigatorio")
    @Email(message = "Formato de email invalido")
    String email,

    @Schema(description = "Senha do usuario", example = "123456")
    @NotBlank(message = "A senha e obrigatoria")
    String senha
) {}
