package br.com.easybiz.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.easybiz.model.Mensagem;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {


    @Query("""
    SELECT m FROM Mensagem m
    JOIN FETCH m.remetente
    WHERE m.pedidoServico.id = :pedidoId
    ORDER BY m.enviadoEm ASC
""")
    List<Mensagem> findByPedidoServico_IdOrderByEnviadoEmAsc(@Param("pedidoId") Long pedidoId);


    @org.springframework.data.jpa.repository.Modifying // Indica que é um UPDATE/DELETE
    @org.springframework.data.jpa.repository.Query("UPDATE Mensagem m SET m.lida = true, m.lidaEm = :data WHERE m.id = :id")
    void marcarMensagemComoLida(@Param("id") Long id, @Param("data") LocalDateTime data);


 // Busca a data da última leitura feita pelo usuário X neste pedido
    // "Quero saber a última vez que este usuário leu uma mensagem que não foi ele que mandou"
    @Query("""
        SELECT MAX(m.lidaEm)
        FROM Mensagem m
        WHERE m.pedidoServico.id = :pedidoId
          AND m.remetente.id <> :usuarioId
          AND m.lida = true
    """)
    LocalDateTime buscarUltimaLeitura(
        @Param("pedidoId") Long pedidoId,
        @Param("usuarioId") Long usuarioId
    );
}

