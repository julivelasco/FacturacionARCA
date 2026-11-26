package com.example.tp4spring.repository;

import com.example.tp4spring.entities.Rubro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RubroRepository extends JpaRepository<Rubro, Long> {

    @Query("SELECT r FROM Rubro r " +
            "WHERE (:incluirBajas = true OR r.fechaBaja IS NULL) " +
            "AND (LOWER(r.denominacion) LIKE LOWER(CONCAT('%', :texto, '%')) " +
            "     OR LOWER(r.codigo) LIKE LOWER(CONCAT('%', :texto, '%')))")
    Page<Rubro> buscar(@Param("texto") String texto,
                       @Param("incluirBajas") boolean incluirBajas,
                       Pageable pageable);
}