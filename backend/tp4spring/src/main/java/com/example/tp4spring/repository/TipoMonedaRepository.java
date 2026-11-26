package com.example.tp4spring.repository;

import com.example.tp4spring.entities.TipoMoneda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoMonedaRepository extends JpaRepository<TipoMoneda, Long> {

    @Query("SELECT t FROM TipoMoneda t " +
            "WHERE (:incluirBajas = true OR t.fechaBaja IS NULL) " +
            "AND (LOWER(t.denominacion) LIKE LOWER(CONCAT('%', :texto, '%')) " +
            "     OR LOWER(t.codigoAfip) LIKE LOWER(CONCAT('%', :texto, '%')))")
    Page<TipoMoneda> buscar(@Param("texto") String texto,
                            @Param("incluirBajas") boolean incluirBajas,
                            Pageable pageable);

    Optional<TipoMoneda> findByCodigoAfip(String codigoAfip);
}