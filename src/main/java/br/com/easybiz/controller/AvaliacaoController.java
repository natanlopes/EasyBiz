package br.com.easybiz.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.easybiz.dto.AvaliacaoDTO;
import br.com.easybiz.dto.AvaliacaoResponseDTO;
import br.com.easybiz.service.AvaliacaoService;
import br.com.easybiz.service.AuthContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller REST para gerenciamento de Avaliações.
 * 
 * <p>Endpoints para criar e consultar avaliações de serviços prestados.</p>
 * 
 * @author EasyBiz Team
 * @since 1.0
 * @see AvaliacaoService
 * @see AvaliacaoResponseDTO
 */
@RestController
@RequestMapping("/avaliacoes")
@Tag(
    name = "Avaliações", 
    description = "Gerenciamento de avaliações de serviços. Permite que clientes avaliem prestadores após a conclusão do serviço."
)
@SecurityRequirement(name = "bearerAuth")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final AuthContextService authContextService;

    public AvaliacaoController(AvaliacaoService avaliacaoService, AuthContextService authContextService) {
        this.avaliacaoService = avaliacaoService;
        this.authContextService = authContextService;
    }

    /**
     * Cria uma avaliação para um pedido concluído.
     * 
     * @param pedidoId ID do pedido a ser avaliado
     * @param dto Dados da avaliação
     * @param principal Usuário autenticado (extraído do JWT)
     * @return Dados da avaliação criada
     */
    @Operation(
        summary = "Avaliar um pedido concluído",
        description = """
            Permite que o **cliente** avalie um serviço após a conclusão.
            
            ## Regras de Negócio
            
            - ✅ O pedido deve estar com status **CONCLUIDO**
            - ✅ Apenas o **cliente** pode avaliar (não o prestador)
            - ✅ Cada pedido só pode ser avaliado **uma única vez**
            - ✅ A nota deve ser de **1 a 5** estrelas
            - ✅ O comentário é **opcional** (máx. 500 caracteres)
            
            ## Efeitos Colaterais
            
            - A **nota média** do negócio é recalculada automaticamente
            - A avaliação fica visível no perfil do prestador
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "✅ Avaliação criada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AvaliacaoResponseDTO.class),
                examples = @ExampleObject(
                    name = "Avaliação 5 estrelas",
                    value = """
                        {
                            "id": 1,
                            "nota": 5,
                            "comentario": "Excelente serviço! Muito profissional.",
                            "dataAvaliacao": "2026-02-05T12:01:06",
                            "pedidoId": 3,
                            "avaliadorNome": "João Silva",
                            "avaliadoNome": "Carlos Barbeiro",
                            "negocioId": 2,
                            "negocioNome": "Barbearia do Carlos"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "❌ Erro de validação",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Pedido não concluído",
                        value = """
                            {
                                "timestamp": "2026-02-05T12:00:00",
                                "status": 400,
                                "error": "Bad Request",
                                "message": "Você só pode avaliar serviços CONCLUÍDOS."
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Já avaliado",
                        value = """
                            {
                                "timestamp": "2026-02-05T12:00:00",
                                "status": 400,
                                "error": "Bad Request",
                                "message": "Este serviço já foi avaliado."
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "❌ Token JWT inválido ou ausente"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "❌ Apenas o cliente pode avaliar",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                            "timestamp": "2026-02-05T12:00:00",
                            "status": 403,
                            "error": "Forbidden",
                            "message": "Apenas o cliente pode avaliar neste momento."
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "❌ Pedido não encontrado"
        )
    })
    @PostMapping("/pedido/{pedidoId}")
    public ResponseEntity<AvaliacaoResponseDTO> avaliar(
            
            @Parameter(
                description = "ID do pedido a ser avaliado",
                example = "3",
                required = true
            )
            @PathVariable Long pedidoId,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados da avaliação",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AvaliacaoDTO.class),
                    examples = @ExampleObject(
                        name = "Avaliação positiva",
                        value = """
                            {
                                "nota": 5,
                                "comentario": "Excelente serviço! Muito profissional e pontual."
                            }
                            """
                    )
                )
            )
            @RequestBody @Valid AvaliacaoDTO dto,
            
            Principal principal
    ) {
        Long usuarioLogadoId = authContextService.getUsuarioIdByEmail(principal.getName());
        
        AvaliacaoResponseDTO response = avaliacaoService.avaliarPedido(pedidoId, usuarioLogadoId, dto);
        
        return ResponseEntity.ok(response);
    }
}