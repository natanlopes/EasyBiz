package br.com.easybiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.easybiz.model.Negocio;

public interface NegocioRepository extends JpaRepository<Negocio, Long> {

    @Query(value = """
        SELECT *
        FROM negocios n
        WHERE
            n.latitude IS NOT NULL AND n.longitude IS NOT NULL
        AND
            (:categoria IS NULL OR UPPER(n.categoria) LIKE UPPER(CONCAT('%', :categoria, '%')))
        AND
            (6371 * acos(
                cos(radians(:userLat)) * cos(radians(n.latitude)) *
                cos(radians(n.longitude) - radians(:userLon)) +
                sin(radians(:userLat)) * sin(radians(n.latitude))
            )) < :raioKm
        ORDER BY n.nota_media DESC
        """, nativeQuery = true)
    List<Negocio> buscarInteligente(
        @Param("userLat") Double userLat,
        @Param("userLon") Double userLon,
        @Param("raioKm") Double raioKm,
        @Param("categoria") String categoria
    );
}

