package br.com.easybiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Dados para atualização do cabeçalho 'Visto por último'")
public record UltimoVistoDTO(

    @Schema(description = "ID do pedido de serviço", example = "1")
    Long pedidoId,

    @Schema(description = "Data/Hora da última mensagem lida por este usuário")
    LocalDateTime vistoEm
) {}