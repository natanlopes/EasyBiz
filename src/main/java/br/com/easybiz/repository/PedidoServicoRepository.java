package br.com.easybiz.repository;

import br.com.easybiz.model.PedidoServico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoServicoRepository extends JpaRepository<PedidoServico, Long> {
}

