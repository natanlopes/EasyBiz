package br.com.easybiz.service;

import br.com.easybiz.dto.CriarPedidoServicoDTO;
import br.com.easybiz.model.*;
import br.com.easybiz.repository.NegocioRepository;
import br.com.easybiz.repository.PedidoServicoRepository;
import br.com.easybiz.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoServicoService {

    private final PedidoServicoRepository pedidoServicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NegocioRepository negocioRepository;

    public PedidoServicoService(
            PedidoServicoRepository pedidoServicoRepository,
            UsuarioRepository usuarioRepository,
            NegocioRepository negocioRepository
    ) {
        this.pedidoServicoRepository = pedidoServicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.negocioRepository = negocioRepository;
    }

    public PedidoServico criarPedido(Long clienteId, CriarPedidoServicoDTO dto) {
        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente (Usuário) não encontrado"));

        Negocio negocio = negocioRepository.findById(dto.negocioId())
                .orElseThrow(() -> new RuntimeException("Negócio não encontrado"));

        PedidoServico pedido = new PedidoServico();
        pedido.setCliente(cliente);
        pedido.setNegocio(negocio);
        pedido.setDescricao(dto.descricao());
        pedido.setDataDesejada(dto.dataDesejada());
        pedido.setStatus(StatusPedido.ABERTO);
        pedido.setCriadoEm(LocalDateTime.now());

        return pedidoServicoRepository.save(pedido);
    }
    public PedidoServico aceitarPedido(Long pedidoId) {
        PedidoServico pedido = pedidoServicoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        // Regra: Só pode aceitar se estiver ABERTO ou negociando
        if (pedido.getStatus() == StatusPedido.CONCLUIDO || 
            pedido.getStatus() == StatusPedido.CANCELADO || 
            pedido.getStatus() == StatusPedido.RECUSADO ||
            pedido.getStatus() == StatusPedido.ACEITO) {
            throw new IllegalStateException("Este pedido não pode mais ser aceito (Status atual: " + pedido.getStatus() + ")");
        }

        pedido.setStatus(StatusPedido.ACEITO);
        // Dica futura: Aqui poderíamos enviar uma Notificação Push para o cliente avisando!
        return pedidoServicoRepository.save(pedido);
    }

    // 2. RECUSAR PEDIDO (O Prestador não pode fazer)
    public PedidoServico recusarPedido(Long pedidoId) {
        PedidoServico pedido = pedidoServicoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (pedido.getStatus() == StatusPedido.CONCLUIDO) {
            throw new IllegalStateException("Não é possível recusar um serviço já finalizado.");
        }

        pedido.setStatus(StatusPedido.RECUSADO);
        return pedidoServicoRepository.save(pedido);
    }

    // 3. CONCLUIR PEDIDO (Serviço feito)
    public PedidoServico concluirPedido(Long pedidoId) {
        PedidoServico pedido = pedidoServicoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        // Regra: O serviço precisava ter sido aceito antes
        if (pedido.getStatus() != StatusPedido.ACEITO) {
            throw new IllegalStateException("O pedido precisa estar ACEITO para ser concluído.");
        }

        pedido.setStatus(StatusPedido.CONCLUIDO);
        return pedidoServicoRepository.save(pedido);
    }


    // Listar pedidos do Cliente (Para a tela "Meus Pedidos")
    public List<PedidoServico> listarPorCliente(Long clienteId) {
        return pedidoServicoRepository.findAllByClienteId(clienteId);
    }

    // Listar pedidos do Prestador (Para a tela "Minha Agenda")
    public List<PedidoServico> listarPorPrestador(Long prestadorId) {
        return pedidoServicoRepository.findAllByNegocioUsuarioId(prestadorId);
    }
}