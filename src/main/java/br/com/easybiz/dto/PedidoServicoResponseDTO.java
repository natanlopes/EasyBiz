package br.com.easybiz.dto;

import java.time.LocalDateTime;

import br.com.easybiz.model.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de retorno de um pedido de serviço (Sem expor senhas)")
public record PedidoServicoResponseDTO(

    @Schema(description = "ID único do pedido", example = "1")
    Long id,

    @Schema(description = "ID do cliente que pediu", example = "2")
    Long clienteId,

    @Schema(description = "Nome do cliente", example = "Ana Souza")
    String clienteNome,

    @Schema(description = "ID do negócio contratado", example = "1")
    Long negocioId,

    @Schema(description = "Nome do negócio contratado", example = "Marcos Elétrica")
    String negocioNome,

    @Schema(description = "Descrição do serviço", example = "Instalar chuveiro")
    String descricao,

    @Schema(description = "Data desejada para o serviço")
    LocalDateTime dataDesejada,

    @Schema(description = "Status atual (ABERTO, ACEITO, CONCLUIDO...)", example = "ABERTO")
    StatusPedido status,

    @Schema(description = "Data de criação do pedido")
    LocalDateTime criadoEm
) {}