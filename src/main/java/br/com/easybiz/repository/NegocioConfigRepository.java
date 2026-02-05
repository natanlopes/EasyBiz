package br.com.easybiz.repository;

import java.util.Optional; // <--- O IMPORT CORRETO É ESSE AQUI

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.easybiz.model.NegocioConfig;

public interface NegocioConfigRepository extends JpaRepository<NegocioConfig, Long> {

    Optional<NegocioConfig> findByNegocioId(Long negocioId);
}