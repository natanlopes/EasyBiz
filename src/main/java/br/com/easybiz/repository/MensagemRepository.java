package br.com.easybiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.easybiz.model.Mensagem;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    List<Mensagem> findByPedidoServico_IdOrderByEnviadoEmAsc(Long pedidoId);
    
 // 🔹 Mensagens NÃO lidas de um pedido (exceto do próprio usuário)
    @Query("""
        SELECT m FROM Mensagem m
        WHERE m.pedidoServico.id = :pedidoId
          AND m.lida = false
          AND m.remetente.id <> :usuarioId
    """)
    List<Mensagem> findNaoLidasDoPedido(
            @Param("pedidoId") Long pedidoId,
            @Param("usuarioId") Long usuarioId
    );
}

