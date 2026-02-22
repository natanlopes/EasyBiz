package br.com.easybiz.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.easybiz.dto.CriarPedidoServicoDTO;
import br.com.easybiz.dto.PedidoServicoResponseDTO;
import br.com.easybiz.exception.BusinessException;
import br.com.easybiz.exception.ForbiddenException;
import br.com.easybiz.exception.ResourceNotFoundException;
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

    public PedidoServicoService(PedidoServicoRepository pedidoRepository,
                                 UsuarioRepository usuarioRepository,
                                 NegocioRepository negocioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.negocioRepository = negocioRepository;
    }

    @Transactional
    public PedidoServicoResponseDTO criar(String emailCliente, CriarPedidoServicoDTO dto) {
        Usuario cliente = usuarioRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente nao encontrado"));

        Negocio negocio = negocioRepository.findById(dto.negocioId())
                .orElseThrow(() -> new ResourceNotFoundException("Negocio nao encontrado"));

        if (negocio.getUsuario().getId().equals(cliente.getId())) {
            throw new BusinessException("Voce nao pode contratar seu proprio servico.");
        }

        PedidoServico pedido = new PedidoServico();
        pedido.setCliente(cliente);
        pedido.setNegocio(negocio);
        pedido.setDescricao(dto.descricao());
        pedido.setDataDesejada(dto.dataDesejada());
        pedido.setStatus(StatusPedido.ABERTO);
        pedido.setCriadoEm(LocalDateTime.now());

        return toDTO(pedidoRepository.save(pedido));
    }

    @Transactional
    public void aceitar(Long pedidoId, Long usuarioLogadoId) {
        PedidoServico pedido = buscarPedido(pedidoId);
        validarDonoDoNegocio(pedido, usuarioLogadoId);

        if (pedido.getStatus() != StatusPedido.ABERTO) {
            throw new BusinessException("Pedido nao esta ABERTO.");
        }
        pedido.setStatus(StatusPedido.ACEITO);
        pedidoRepository.save(pedido);
    }

    @Transactional
    public void recusar(Long pedidoId, Long usuarioLogadoId) {
        PedidoServico pedido = buscarPedido(pedidoId);
        validarDonoDoNegocio(pedido, usuarioLogadoId);

        if (pedido.getStatus() != StatusPedido.ABERTO) {
            throw new BusinessException("Apenas pedidos ABERTOS podem ser recusados.");
        }
        pedido.setStatus(StatusPedido.RECUSADO);
        pedidoRepository.save(pedido);
    }


    @Transactional
    public void concluir(Long pedidoId, Long usuarioLogadoId) {
        PedidoServico pedido = buscarPedido(pedidoId);
        validarDonoDoNegocio(pedido, usuarioLogadoId);

        if (pedido.getStatus() != StatusPedido.ACEITO) {
            throw new BusinessException("Pedido precisa estar ACEITO.");
        }
        pedido.setStatus(StatusPedido.CONCLUIDO);
        pedidoRepository.save(pedido);
    }

    @Transactional
    public void cancelar(Long pedidoId, Long clienteId) {
        PedidoServico pedido = buscarPedido(pedidoId);

        if (!pedido.getCliente().getId().equals(clienteId)) {
            throw new ForbiddenException("Apenas o cliente pode cancelar.");
        }

        if (pedido.getStatus() == StatusPedido.CONCLUIDO ||
                pedido.getStatus() == StatusPedido.CANCELADO ||
                pedido.getStatus() == StatusPedido.RECUSADO) {
            throw new BusinessException("Este pedido nao pode mais ser cancelado.");
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }


    public Page<PedidoServicoResponseDTO> listarMeusPedidos(Long usuarioId, Pageable pageable) {
        return pedidoRepository.findByClienteIdOrNegocioUsuarioId(usuarioId, usuarioId, pageable)
                .map(this::toDTO);
    }

    private PedidoServico buscarPedido(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido nao encontrado"));
    }

    private void validarDonoDoNegocio(PedidoServico pedido, Long usuarioId) {
        if (!pedido.getNegocio().getUsuario().getId().equals(usuarioId)) {
            throw new ForbiddenException("Acesso negado: Apenas o prestador pode fazer isso.");
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
