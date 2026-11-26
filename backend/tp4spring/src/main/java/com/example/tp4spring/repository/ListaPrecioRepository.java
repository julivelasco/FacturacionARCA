package com.example.tp4spring.repository;

import com.example.tp4spring.entities.ListaPrecio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ListaPrecioRepository extends JpaRepository<ListaPrecio, Long> {

    @Query("SELECT l FROM ListaPrecio l " +
            "WHERE (:incluirBajas = true OR l.fechaBaja IS NULL) " +
            "AND (LOWER(l.denominacion) LIKE LOWER(CONCAT('%', :texto, '%')) " +
            "     OR LOWER(l.codigo) LIKE LOWER(CONCAT('%', :texto, '%')))")
    Page<ListaPrecio> buscar(@Param("texto") String texto,
                             @Param("incluirBajas") boolean incluirBajas,
                             Pageable pageable);
}