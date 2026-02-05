package br.com.easybiz.dto;

import org.hibernate.validator.constraints.URL;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AtualizarFotoDTO(
    @Schema(description = "URL pública da imagem (Firebase, S3, etc)", example = "https://meubucket.com/avatar.jpg")
    @NotBlank(message = "A URL não pode estar vazia")
    @URL(message = "Deve ser uma URL válida") // Validação extra de formato
    String url
) {}