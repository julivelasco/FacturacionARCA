package com.example.tp4spring.repository;

import com.example.tp4spring.entities.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // El filtro del buscador: lo comparten las tres consultas
    String FILTRO = "WHERE (:incluirBajas = true OR c.fechaBaja IS NULL) " +
            "AND (LOWER(c.denominacion) LIKE LOWER(CONCAT('%', :texto, '%')) " +
            "     OR c.cuitCuil LIKE CONCAT('%', :texto, '%')) ";

    // Las compras de un cliente son sus facturas EMITIDAS. LEFT JOIN: los que nunca compraron también aparecen
    String CON_COMPRAS = "SELECT c FROM Cliente c " +
            "LEFT JOIN FacturaVenta f ON f.cliente = c AND f.estado = 'EMITIDA' ";

    // Para la paginación: cuántos clientes cumplen el filtro (sin agrupar)
    String CONTAR = "SELECT COUNT(c) FROM Cliente c " + FILTRO;

    @Query("SELECT c FROM Cliente c " + FILTRO)
    Page<Cliente> buscar(@Param("texto") String texto,
                         @Param("incluirBajas") boolean incluirBajas,
                         Pageable pageable);

    @Query(value = CON_COMPRAS + FILTRO + "GROUP BY c ORDER BY COUNT(f) DESC, c.denominacion",
            countQuery = CONTAR)
    Page<Cliente> buscarPorCantidadDeCompras(@Param("texto") String texto,
                                             @Param("incluirBajas") boolean incluirBajas,
                                             Pageable pageable);

    @Query(value = CON_COMPRAS + FILTRO + "GROUP BY c ORDER BY MAX(f.fechaEmision) DESC NULLS LAST, c.denominacion",
            countQuery = CONTAR)
    Page<Cliente> buscarPorCompraMasReciente(@Param("texto") String texto,
                                             @Param("incluirBajas") boolean incluirBajas,
                                             Pageable pageable);

    Optional<Cliente> findByCuitCuil(String cuitCuil);
}