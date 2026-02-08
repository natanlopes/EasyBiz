package br.com.easybiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para login")
public record LoginRequestDTO(
    @Schema(description = "E-mail do usuário", example = "admin@email.com")
    String email,
    
    @Schema(description = "Senha do usuário", example = "123456")
    String senha
) {}