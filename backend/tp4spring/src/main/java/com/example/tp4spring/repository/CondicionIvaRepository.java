package com.example.tp4spring.repository;

import com.example.tp4spring.entities.CondicionIva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CondicionIvaRepository extends JpaRepository<CondicionIva, Long> {

    @Query("SELECT c FROM CondicionIva c " +
            "WHERE (:incluirBajas = true OR c.fechaBaja IS NULL) " +
            "AND (LOWER(c.denominacion) LIKE LOWER(CONCAT('%', :texto, '%')) " +
            "     OR CAST(c.codigoAfip AS String) LIKE CONCAT('%', :texto, '%'))")
    Page<CondicionIva> buscar(@Param("texto") String texto,
                              @Param("incluirBajas") boolean incluirBajas,
                              Pageable pageable);

    Optional<CondicionIva> findByCodigoAfip(int codigoAfip);
}