package br.com.easybiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados necessarios para criacao de um negocio")
public record CriarNegocioDTO(
        @Schema(example = "EasyBiz Barbearia", description = "Nome do negocio")
        @NotBlank String nome,

        @Schema(example = "BARBEARIA", description = "Categoria do servico (Ex: Pedreiro, Manicure)")
        @NotBlank String categoria
) {}
