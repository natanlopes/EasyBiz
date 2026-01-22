package br.com.easybiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.easybiz.model.Mensagem;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    List<Mensagem> findByPedidoServico_IdOrderByEnviadoEmAsc(Long pedidoId);
}

