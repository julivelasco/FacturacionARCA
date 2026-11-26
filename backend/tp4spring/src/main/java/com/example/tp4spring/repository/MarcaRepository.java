package com.example.tp4spring.repository;

import com.example.tp4spring.entities.Marca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MarcaRepository extends JpaRepository<Marca, Long> {

    @Query("SELECT m FROM Marca m " +
            "WHERE (:incluirBajas = true OR m.fechaBaja IS NULL) " +
            "AND (LOWER(m.denominacion) LIKE LOWER(CONCAT('%', :texto, '%')) " +
            "     OR LOWER(m.codigo) LIKE LOWER(CONCAT('%', :texto, '%')))")
    Page<Marca> buscar(@Param("texto") String texto,
                       @Param("incluirBajas") boolean incluirBajas,
                       Pageable pageable);
}