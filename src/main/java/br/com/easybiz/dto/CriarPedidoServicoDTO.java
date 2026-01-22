package br.com.easybiz.dto;

import java.time.LocalDateTime;

public record CriarPedidoServicoDTO(
    Long negocioId,
    String descricao,
    LocalDateTime dataDesejada
) {}
