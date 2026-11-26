package com.example.tp4spring.repository;

import com.example.tp4spring.entities.ListaPrecioArticulo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ListaPrecioArticuloRepository extends JpaRepository<ListaPrecioArticulo, Long> {

    @Query("SELECT lpa FROM ListaPrecioArticulo lpa " +
            "JOIN lpa.articulo a " +
            "JOIN lpa.listaPrecio l " +
            "WHERE (:incluirBajas = true OR " +
            "       (lpa.fechaBaja IS NULL AND a.fechaBaja IS NULL AND l.fechaBaja IS NULL)) " +
            "AND (LOWER(a.denominacion) LIKE LOWER(CONCAT('%', :texto, '%')) " +
            "     OR LOWER(a.codigo) LIKE LOWER(CONCAT('%', :texto, '%')) " +
            "     OR LOWER(l.denominacion) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
            "AND (:listaPrecioId IS NULL OR l.id = :listaPrecioId)")
    Page<ListaPrecioArticulo> buscar(@Param("texto") String texto,
                                     @Param("incluirBajas") boolean incluirBajas,
                                     @Param("listaPrecioId") Long listaPrecioId,
                                     Pageable pageable);

    Optional<ListaPrecioArticulo> findByListaPrecioIdAndArticuloId(Long listaPrecioId, Long articuloId);
}