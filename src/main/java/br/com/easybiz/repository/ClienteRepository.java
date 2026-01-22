package br.com.easybiz.repository;

import br.com.easybiz.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByNegocioId(Long negocioId);
}

