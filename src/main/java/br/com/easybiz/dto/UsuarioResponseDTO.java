package br.com.easybiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados públicos do usuário para exibição no App")
public record UsuarioResponseDTO(

    @Schema(example = "1")
    Long id,

    @Schema(example = "Marcos Silva")
    String nome,

    @Schema(example = "marcos@email.com")
    String email,

    @Schema(example = "https://cdn.easybiz.com/fotos/marcos.jpg")
    String fotoUrl
) {}