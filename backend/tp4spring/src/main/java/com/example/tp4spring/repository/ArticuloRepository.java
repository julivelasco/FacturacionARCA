package com.example.tp4spring.repository;

import com.example.tp4spring.entities.Articulo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticuloRepository extends JpaRepository<Articulo, Long> {

    String FILTRO = "WHERE (:incluirBajas = true OR a.fechaBaja IS NULL) " +
            "AND (LOWER(a.denominacion) LIKE LOWER(CONCAT('%', :texto, '%')) " +
            "     OR LOWER(a.codigo) LIKE LOWER(CONCAT('%', :texto, '%'))) ";

    String CONTAR = "SELECT COUNT(a) FROM Articulo a " + FILTRO;

    @Query("SELECT a FROM Articulo a " + FILTRO)
    Page<Articulo> buscar(@Param("texto") String texto,
                          @Param("incluirBajas") boolean incluirBajas,
                          Pageable pageable);

    // LEFT JOIN: los artículos sin rubro también aparecen (al final, por el NULLS LAST)
    @Query(value = "SELECT a FROM Articulo a LEFT JOIN a.rubro r " + FILTRO +
            "ORDER BY r.denominacion NULLS LAST, a.denominacion",
            countQuery = CONTAR)
    Page<Articulo> buscarPorRubro(@Param("texto") String texto,
                                  @Param("incluirBajas") boolean incluirBajas,
                                  Pageable pageable);

    @Query(value = "SELECT a FROM Articulo a LEFT JOIN a.marca m " + FILTRO +
            "ORDER BY m.denominacion NULLS LAST, a.denominacion",
            countQuery = CONTAR)
    Page<Articulo> buscarPorMarca(@Param("texto") String texto,
                                  @Param("incluirBajas") boolean incluirBajas,
                                  Pageable pageable);

    Optional<Articulo> findByCodigo(String codigo);
}