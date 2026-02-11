package br.com.easybiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados necessários para criação de um negócio")
public record CriarNegocioDTO(
        @Schema(example = "EasyBiz Barbearia", description = "Nome do negócio")
        @NotBlank String nome,

        @Schema(example = "BARBEARIA", description = "Categoria do serviço (Ex: Pedreiro, Manicure, Python Dev)")
        @NotBlank String categoria

) {

    public String nome() {
        return nome;
    }

    public String tipo() {
        return categoria;
    }
}
