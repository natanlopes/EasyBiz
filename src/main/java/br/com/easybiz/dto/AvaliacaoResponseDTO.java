package br.com.easybiz.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de resposta para Avaliação.
 * Retorna apenas os dados necessários, sem expor informações sensíveis.
 * 
 * @author EasyBiz Team
 * @since 1.0
 * @see br.com.easybiz.model.Avaliacao
 */
@Schema(
    name = "AvaliacaoResponse",
    description = "Dados de retorno de uma avaliação de serviço"
)
public record AvaliacaoResponseDTO(
    
    @Schema(
        description = "ID único da avaliação",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Long id,
    
    @Schema(
        description = "Nota dada ao serviço (1 a 5 estrelas)",
        example = "5",
        minimum = "1",
        maximum = "5",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer nota,
    
    @Schema(
        description = "Comentário opcional sobre o serviço",
        example = "Excelente serviço! Muito profissional.",
        maxLength = 500,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String comentario,
    
    @Schema(
        description = "Data e hora em que a avaliação foi realizada",
        example = "2026-02-05T12:01:06",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    LocalDateTime dataAvaliacao,
    
    @Schema(
        description = "ID do pedido avaliado",
        example = "3",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Long pedidoId,
    
    @Schema(
        description = "Nome do cliente que fez a avaliação",
        example = "João Silva",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String avaliadorNome,
    
    @Schema(
        description = "Nome do prestador que foi avaliado",
        example = "Carlos Barbeiro",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String avaliadoNome,
    
    @Schema(
        description = "ID do negócio avaliado",
        example = "2",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Long negocioId,
    
    @Schema(
        description = "Nome do negócio avaliado",
        example = "Barbearia do Carlos",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String negocioNome

) {
    
    /**
     * Factory method para criar o DTO a partir da entidade Avaliacao.
     * Converte a entidade completa em um DTO seguro para exposição na API.
     * 
     * @param avaliacao Entidade Avaliacao do banco de dados
     * @return AvaliacaoResponseDTO com dados sanitizados
     */
    public static AvaliacaoResponseDTO fromEntity(br.com.easybiz.model.Avaliacao avaliacao) {
        return new AvaliacaoResponseDTO(
            avaliacao.getId(),
            avaliacao.getNota(),
            avaliacao.getComentario(),
            avaliacao.getDataAvaliacao(),
            avaliacao.getPedido().getId(),
            avaliacao.getAvaliador().getNomeCompleto(),
            avaliacao.getAvaliado().getNomeCompleto(),
            avaliacao.getPedido().getNegocio().getId(),
            avaliacao.getPedido().getNegocio().getNome()
        );
    }
}