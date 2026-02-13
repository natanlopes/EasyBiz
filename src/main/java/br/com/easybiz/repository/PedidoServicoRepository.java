package br.com.easybiz.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.easybiz.model.PedidoServico;

public interface PedidoServicoRepository extends JpaRepository<PedidoServico, Long> {

    Page<PedidoServico> findByClienteIdOrNegocioUsuarioId(Long clienteId, Long prestadorId, Pageable pageable);
}

