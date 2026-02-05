package br.com.easybiz.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.easybiz.dto.CriarPedidoServicoDTO;
import br.com.easybiz.dto.PedidoServicoResponseDTO; // Importe o DTO
import br.com.easybiz.model.Negocio;
import br.com.easybiz.model.PedidoServico;
import br.com.easybiz.model.StatusPedido;
import br.com.easybiz.model.Usuario;
import br.com.easybiz.repository.NegocioRepository;
import br.com.easybiz.repository.PedidoServicoRepository;
import br.com.easybiz.repository.UsuarioRepository;

@Service
public class PedidoServicoService {

    private final PedidoServicoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NegocioRepository negocioRepository;

    public PedidoServicoService(PedidoServicoRepository pedidoRepository, UsuarioRepository usuarioRepository, NegocioRepository negocioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.negocioRepository = negocioRepository;
    }

    // CRIAR
    @Transactional
    public PedidoServicoResponseDTO criar(Long clienteId, CriarPedidoServicoDTO dto) {
        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Negocio negocio = negocioRepository.findById(dto.negocioId())
                .orElseThrow(() -> new RuntimeException("Negócio não encontrado"));

        if (negocio.getUsuario().getId().equals(clienteId)) {
             throw new RuntimeException("Você não pode contratar seu próprio serviço.");
        }

        PedidoServico pedido = new PedidoServico();
        pedido.setCliente(cliente);
        pedido.setNegocio(negocio);
        pedido.setDescricao(dto.descricao());
        pedido.setDataDesejada(dto.dataDesejada());
        pedido.setStatus(StatusPedido.ABERTO);
        pedido.setCriadoEm(LocalDateTime.now());

        return toDTO(pedidoRepository.save(pedido)); // Retorna DTO seguro
    }

    // ACEITAR
    @Transactional
    public void aceitar(Long pedidoId, Long usuarioLogadoId) {
        PedidoServico pedido = buscarPedido(pedidoId);
        validarDonoDoNegocio(pedido, usuarioLogadoId);

        if (pedido.getStatus() != StatusPedido.ABERTO) {
            throw new IllegalStateException("Pedido não está ABERTO.");
        }
        pedido.setStatus(StatusPedido.ACEITO);
        pedidoRepository.save(pedido);
    }

    // RECUSAR
    @Transactional
    public void recusar(Long pedidoId, Long usuarioLogadoId) {
        PedidoServico pedido = buscarPedido(pedidoId);
        validarDonoDoNegocio(pedido, usuarioLogadoId);

        if (pedido.getStatus() == StatusPedido.CONCLUIDO) {
            throw new IllegalStateException("Não pode recusar serviço concluído.");
        }
        pedido.setStatus(StatusPedido.RECUSADO);
        pedidoRepository.save(pedido);
    }

    // CONCLUIR
    @Transactional
    public void concluir(Long pedidoId, Long usuarioLogadoId) {
        PedidoServico pedido = buscarPedido(pedidoId);
        validarDonoDoNegocio(pedido, usuarioLogadoId);

        if (pedido.getStatus() != StatusPedido.ACEITO) {
            throw new IllegalStateException("Pedido precisa estar ACEITO.");
        }
        pedido.setStatus(StatusPedido.CONCLUIDO);
        pedidoRepository.save(pedido);
    }
    //
    @Transactional
    public void cancelar(Long pedidoId, Long clienteId) {
        PedidoServico pedido = buscarPedido(pedidoId);
        
        // Só o cliente pode cancelar
        if (!pedido.getCliente().getId().equals(clienteId)) {
            throw new RuntimeException("Apenas o cliente pode cancelar.");
        }
        
        // Não pode cancelar se já foi concluído
        if (pedido.getStatus() == StatusPedido.CONCLUIDO) {
            throw new IllegalStateException("Não é possível cancelar serviço já concluído.");
        }
        
        pedido.setStatus(StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }

    // LISTAR MEUS PEDIDOS (Inteligente: serve para Cliente e Prestador)
    public List<PedidoServicoResponseDTO> listarMeusPedidos(Long usuarioId) {
        return pedidoRepository.findAll().stream()
                .filter(p -> p.getCliente().getId().equals(usuarioId) ||
                             p.getNegocio().getUsuario().getId().equals(usuarioId))
                .map(this::toDTO)
                .toList();
    }

    // --- Auxiliares ---

    private PedidoServico buscarPedido(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }

    private void validarDonoDoNegocio(PedidoServico pedido, Long usuarioId) {
        if (!pedido.getNegocio().getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("Acesso negado: Apenas o prestador pode fazer isso.");
        }
    }

    private PedidoServicoResponseDTO toDTO(PedidoServico pedido) {
        return new PedidoServicoResponseDTO(
                pedido.getId(),
                pedido.getCliente().getId(),
                pedido.getCliente().getNomeCompleto(),
                pedido.getNegocio().getId(),
                pedido.getNegocio().getNome(),
                pedido.getDescricao(),
                pedido.getDataDesejada(),
                pedido.getStatus(),
                pedido.getCriadoEm()
        );
    }
}