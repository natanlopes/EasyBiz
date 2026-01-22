package br.com.easybiz.repository;

import br.com.easybiz.model.NegocioConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NegocioConfigRepository extends JpaRepository<NegocioConfig, Long> {

    Optional<NegocioConfig> findByNegocioId(Long negocioId);
}

