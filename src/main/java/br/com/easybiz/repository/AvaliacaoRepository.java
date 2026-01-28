package br.com.easybiz.repository;

import br.com.easybiz.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    // Para buscar todas as avaliações de um prestador (ex: para mostrar no perfil dele)
    List<Avaliacao> findByAvaliadoId(Long usuarioId);
    
    // Para saber se esse pedido já foi avaliado (evitar duplicidade)
    boolean existsByPedidoId(Long pedidoId);
}