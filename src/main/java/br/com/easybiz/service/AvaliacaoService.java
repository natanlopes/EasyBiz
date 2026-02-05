package br.com.easybiz.service;

import org.springframework.stereotype.Service;

import br.com.easybiz.dto.AvaliacaoDTO;
import br.com.easybiz.model.Avaliacao;
import br.com.easybiz.model.Negocio;
import br.com.easybiz.model.PedidoServico;
import br.com.easybiz.model.StatusPedido;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.AvaliacaoRepository;
import br.com.easybiz.repository.NegocioRepository;
import br.com.easybiz.repository.PedidoServicoRepository;
import jakarta.transaction.Transactional;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final PedidoServicoRepository pedidoRepository;
    private final NegocioRepository negocioRepository;

	public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, PedidoServicoRepository pedidoRepository,
			NegocioRepository negocioRepository) {
		this.avaliacaoRepository = avaliacaoRepository;
		this.pedidoRepository = pedidoRepository;
		this.negocioRepository = negocioRepository;
	}
	@Transactional
    public Avaliacao avaliarPedido(Long pedidoId, Long usuarioLogadoId, AvaliacaoDTO dto) {
        PedidoServico pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        // 1. Validação de Status
        if (pedido.getStatus() != StatusPedido.CONCLUIDO) {
            throw new IllegalStateException("Você só pode avaliar serviços CONCLUÍDOS.");
        }

        // 2. Validação de Duplicidade (Não pode avaliar 2 vezes o mesmo serviço)
        if (avaliacaoRepository.existsByPedidoId(pedidoId)) {
            throw new IllegalStateException("Este serviço já foi avaliado.");
        }

        // 3. Verifica quem está avaliando
        Usuario avaliador;
        Usuario avaliado;

        if (pedido.getCliente().getId().equals(usuarioLogadoId)) {
            // Se sou o cliente, estou avaliando o dono do negócio
            avaliador = pedido.getCliente();
            avaliado = pedido.getNegocio().getUsuario();
        } else {
            // (Opcional) Poderíamos permitir o prestador avaliar o cliente também futuramente
            throw new IllegalStateException("Apenas o cliente pode avaliar neste momento.");
        }

        // 4. Salva
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setPedido(pedido);
        avaliacao.setAvaliador(avaliador);
        avaliacao.setAvaliado(avaliado);
        avaliacao.setNota(dto.nota());
        avaliacao.setComentario(dto.comentario());

        avaliacao = avaliacaoRepository.save(avaliacao);

        Negocio negocio = pedido.getNegocio();
        Double novaMedia = avaliacaoRepository.calcularMediaDoNegocio(negocio.getId());

        negocio.setNotaMedia(novaMedia); // Atualiza o campo cached
        negocioRepository.save(negocio); // Grava no banco

        return avaliacao;
    }
}
