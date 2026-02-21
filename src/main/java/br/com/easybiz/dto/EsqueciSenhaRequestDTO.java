package br.com.easybiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para solicitar recuperacao de senha")
public record EsqueciSenhaRequestDTO(
    @Schema(description = "E-mail cadastrado", example = "usuario@email.com")
    @NotBlank(message = "O email e obrigatorio")
    @Email(message = "Formato de email invalido")
    @Size(max=150)
    String email
) {}
