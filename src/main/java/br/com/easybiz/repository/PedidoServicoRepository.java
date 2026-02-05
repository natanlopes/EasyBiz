package br.com.easybiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.easybiz.model.PedidoServico;

public interface PedidoServicoRepository extends JpaRepository<PedidoServico, Long> {
	// Para o Cliente ver o histórico dele
    List<PedidoServico> findAllByClienteId(Long clienteId);

    // Para o Prestador ver os pedidos do negócio dele
    // Note que o pedido está ligado ao Negocio, não direto ao Usuario prestador
    List<PedidoServico> findAllByNegocioUsuarioId(Long usuarioId);
}

