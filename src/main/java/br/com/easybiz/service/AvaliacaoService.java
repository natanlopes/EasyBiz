package br.com.easybiz.service;

import org.springframework.stereotype.Service;

import br.com.easybiz.dto.AvaliacaoDTO;
import br.com.easybiz.dto.AvaliacaoResponseDTO;
import br.com.easybiz.model.Avaliacao;
import br.com.easybiz.model.Negocio;
import br.com.easybiz.model.PedidoServico;
import br.com.easybiz.model.StatusPedido;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.AvaliacaoRepository;
import br.com.easybiz.repository.NegocioRepository;
import br.com.easybiz.repository.PedidoServicoRepository;
import jakarta.transaction.Transactional;

/**
 * Serviço responsável pela lógica de negócio das Avaliações.
 * 
 * <p>Gerencia o ciclo de vida das avaliações de serviços, incluindo:</p>
 * <ul>
 *   <li>Criação de avaliações após conclusão do pedido</li>
 *   <li>Validação de permissões (apenas cliente pode avaliar)</li>
 *   <li>Atualização automática da nota média do negócio</li>
 * </ul>
 * 
 * @author EasyBiz Team
 * @since 1.0
 * @see AvaliacaoRepository
 * @see AvaliacaoResponseDTO
 */
@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final PedidoServicoRepository pedidoRepository;
    private final NegocioRepository negocioRepository;

    public AvaliacaoService(
            AvaliacaoRepository avaliacaoRepository, 
            PedidoServicoRepository pedidoRepository,
            NegocioRepository negocioRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.pedidoRepository = pedidoRepository;
        this.negocioRepository = negocioRepository;
    }

    /**
     * Cria uma avaliação para um pedido concluído.
     * 
     * <p><b>Regras de negócio:</b></p>
     * <ul>
     *   <li>Pedido deve estar com status CONCLUIDO</li>
     *   <li>Apenas o cliente pode avaliar (por enquanto)</li>
     *   <li>Cada pedido só pode ser avaliado uma vez</li>
     *   <li>A nota média do negócio é recalculada automaticamente</li>
     * </ul>
     * 
     * @param pedidoId ID do pedido a ser avaliado
     * @param usuarioLogadoId ID do usuário que está fazendo a avaliação (extraído do JWT)
     * @param dto Dados da avaliação (nota e comentário)
     * @return AvaliacaoResponseDTO com os dados da avaliação criada
     * @throws RuntimeException se o pedido não for encontrado
     * @throws IllegalStateException se o pedido não estiver CONCLUIDO
     * @throws IllegalStateException se o pedido já foi avaliado
     * @throws IllegalStateException se quem está avaliando não é o cliente
     */
    @Transactional
    public AvaliacaoResponseDTO avaliarPedido(Long pedidoId, Long usuarioLogadoId, AvaliacaoDTO dto) {
        
        // 1. Busca o pedido
        PedidoServico pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        // 2. Validação de Status
        if (pedido.getStatus() != StatusPedido.CONCLUIDO) {
            throw new IllegalStateException("Você só pode avaliar serviços CONCLUÍDOS.");
        }

        // 3. Validação de Duplicidade
        if (avaliacaoRepository.existsByPedidoId(pedidoId)) {
            throw new IllegalStateException("Este serviço já foi avaliado.");
        }

        // 4. Verifica quem está avaliando
        Usuario avaliador;
        Usuario avaliado;

        if (pedido.getCliente().getId().equals(usuarioLogadoId)) {
            avaliador = pedido.getCliente();
            avaliado = pedido.getNegocio().getUsuario();
        } else {
            throw new IllegalStateException("Apenas o cliente pode avaliar neste momento.");
        }

        // 5. Cria e salva a avaliação
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setPedido(pedido);
        avaliacao.setAvaliador(avaliador);
        avaliacao.setAvaliado(avaliado);
        avaliacao.setNota(dto.nota());
        avaliacao.setComentario(dto.comentario());

        avaliacao = avaliacaoRepository.save(avaliacao);

        // 6. Atualiza nota média do negócio (cached para performance)
        atualizarNotaMediaNegocio(pedido.getNegocio());

        // 7. Retorna DTO seguro (sem dados sensíveis)
        return AvaliacaoResponseDTO.fromEntity(avaliacao);
    }
    
    /**
     * Recalcula e atualiza a nota média de um negócio.
     * 
     * <p>A nota média é mantida em cache na entidade Negocio para
     * otimizar queries de busca e ranking.</p>
     * 
     * @param negocio Negócio a ter a nota recalculada
     */
    private void atualizarNotaMediaNegocio(Negocio negocio) {
        Double novaMedia = avaliacaoRepository.calcularMediaDoNegocio(negocio.getId());
        negocio.setNotaMedia(novaMedia != null ? novaMedia : 0.0);
        negocioRepository.save(negocio);
    }
}