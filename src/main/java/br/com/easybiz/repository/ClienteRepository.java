package br.com.easybiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.easybiz.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByNegocioId(Long negocioId);
}

