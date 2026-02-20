package br.com.easybiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EnviarMensagemDTO(
        // REMOVIDO: usuarioId (Segurança: agora pegamos do Token)
//        @Schema(description = "ID do usuário que está enviando (Cliente ou Dono)", example = "1")
//        @NotNull
//        Long usuarioId,

        @Schema(description = "Texto da mensagem", example = "Olá, qual o valor do orçamento?")
        @NotBlank @Size(max = 2000)
        String conteudo
) {}
