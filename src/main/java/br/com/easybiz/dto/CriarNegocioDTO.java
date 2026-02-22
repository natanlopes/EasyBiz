package br.com.easybiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados necessarios para criacao de um negocio")
public record CriarNegocioDTO(
        @Schema(example = "EasyBiz Barbearia", description = "Nome do negocio")
        @NotBlank @Size(max = 100) String nome,

        @Schema(example = "BARBEARIA", description = "Categoria do servico (Ex: Pedreiro, Manicure)")
        @NotBlank @Size(max = 50) String categoria,

        Double latitude,
        Double longitude,
        @Size(max = 255) String enderecoCompleto
) {}
