package br.com.easybiz.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.easybiz.model.PedidoServico;

public interface PedidoServicoRepository extends JpaRepository<PedidoServico, Long> {

    @Query("""
        SELECT p FROM PedidoServico p
        JOIN FETCH p.cliente
        JOIN FETCH p.negocio n
        JOIN FETCH n.usuario
        WHERE p.cliente.id = :clienteId OR n.usuario.id = :prestadorId
    """)
    Page<PedidoServico> findByClienteIdOrNegocioUsuarioId(
            @Param("clienteId") Long clienteId,
            @Param("prestadorId") Long prestadorId,
            Pageable pageable
    );
}

