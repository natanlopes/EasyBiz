package br.com.easybiz.service;

import br.com.easybiz.dto.CriarPedidoServicoDTO;
import br.com.easybiz.model.*;
import br.com.easybiz.repository.NegocioRepository;
import br.com.easybiz.repository.PedidoServicoRepository;
import br.com.easybiz.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
}