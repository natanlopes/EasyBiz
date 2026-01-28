package br.com.easybiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados necessários para avaliação")
public record AvaliacaoDTO(@Schema(example = "1", description = "Nota da avaliação") Integer nota,
		@Schema(example = "comentario", description = "Comentario sobre o serviço") String comentario) {

}