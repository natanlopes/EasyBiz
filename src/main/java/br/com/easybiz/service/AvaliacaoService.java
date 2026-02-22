package br.com.easybiz.service;

import org.springframework.stereotype.Service;

import br.com.easybiz.dto.AvaliacaoDTO;
import br.com.easybiz.dto.AvaliacaoResponseDTO;
import br.com.easybiz.exception.BusinessException;
import br.com.easybiz.exception.ForbiddenException;
import br.com.easybiz.exception.ResourceNotFoundException;
import br.com.easybiz.model.Avaliacao;
import br.com.easybiz.model.Negocio;
import br.com.easybiz.model.PedidoServico;
import br.com.easybiz.model.StatusPedido;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.AvaliacaoRepository;
import br.com.easybiz.repository.NegocioRepository;
import br.com.easybiz.repository.PedidoServicoRepository;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public AvaliacaoResponseDTO avaliarPedido(Long pedidoId, Long usuarioLogadoId, AvaliacaoDTO dto) {
        PedidoServico pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido nao encontrado"));

        if (pedido.getStatus() != StatusPedido.CONCLUIDO) {
            throw new BusinessException("Voce so pode avaliar servicos CONCLUIDOS.");
        }

        if (avaliacaoRepository.existsByPedidoId(pedidoId)) {
            throw new BusinessException("Este servico ja foi avaliado.");
        }

        Usuario avaliador;
        Usuario avaliado;

        if (pedido.getCliente().getId().equals(usuarioLogadoId)) {
            avaliador = pedido.getCliente();
            avaliado = pedido.getNegocio().getUsuario();
        } else {
            throw new ForbiddenException("Apenas o cliente pode avaliar neste momento.");
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setPedido(pedido);
        avaliacao.setAvaliador(avaliador);
        avaliacao.setAvaliado(avaliado);
        avaliacao.setNota(dto.nota());
        avaliacao.setComentario(dto.comentario());

        avaliacao = avaliacaoRepository.save(avaliacao);

        atualizarNotaMediaNegocio(pedido.getNegocio());

        return AvaliacaoResponseDTO.fromEntity(avaliacao);
    }

    private void atualizarNotaMediaNegocio(Negocio negocio) {
        Double novaMedia = avaliacaoRepository.calcularMediaDoNegocio(negocio.getId());
        negocio.setNotaMedia(novaMedia != null ? novaMedia : 0.0);
        negocioRepository.save(negocio);
    }
}
