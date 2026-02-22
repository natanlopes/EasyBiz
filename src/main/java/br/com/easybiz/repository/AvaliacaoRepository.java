package br.com.easybiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.easybiz.model.Avaliacao;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    boolean existsByPedidoId(Long pedidoId);

 //  CÁLCULO DE MÉDIA (Query JPQL)
    // "Selecione a Média (AVG) das notas onde o negócio do pedido seja X"
    // O COALESCE serve para retornar 0.0 se não tiver nenhuma nota (evita null)
    @Query("SELECT COALESCE(AVG(a.nota), 0.0) FROM Avaliacao a WHERE a.pedido.negocio.id = :negocioId")
    Double calcularMediaDoNegocio(@Param("negocioId") Long negocioId);
}