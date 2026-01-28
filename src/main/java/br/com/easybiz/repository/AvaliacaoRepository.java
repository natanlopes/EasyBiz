package br.com.easybiz.repository;

import br.com.easybiz.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    // Para buscar todas as avaliações de um prestador (ex: para mostrar no perfil dele)
    List<Avaliacao> findByAvaliadoId(Long usuarioId);
    
    // Para saber se esse pedido já foi avaliado (evitar duplicidade)
    boolean existsByPedidoId(Long pedidoId);
    
 //  CÁLCULO DE MÉDIA (Query JPQL)
    // "Selecione a Média (AVG) das notas onde o negócio do pedido seja X"
    // O COALESCE serve para retornar 0.0 se não tiver nenhuma nota (evita null)
    @Query("SELECT COALESCE(AVG(a.nota), 0.0) FROM Avaliacao a WHERE a.pedido.negocio.id = :negocioId")
    Double calcularMediaDoNegocio(@Param("negocioId") Long negocioId);
}