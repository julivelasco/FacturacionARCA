package com.example.tp4spring.repository;

import com.example.tp4spring.entities.PuntoVenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PuntoVentaRepository extends JpaRepository<PuntoVenta, Long> {
    @Query("SELECT p FROM PuntoVenta p " +
            "WHERE (:incluirBajas = true OR p.fechaBaja IS NULL) " +
            "AND (LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%')) " +
            "     OR CAST(p.numero AS String) LIKE CONCAT('%', :texto, '%'))")
    Page<PuntoVenta> buscar(@Param("texto") String texto,
                            @Param("incluirBajas") boolean incluirBajas,
                            Pageable pageable);

    Optional<PuntoVenta> findByNumero(int numero);
}
