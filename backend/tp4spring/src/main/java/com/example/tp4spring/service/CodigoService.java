package com.example.tp4spring.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CodigoService {

    private final EntityManager em;

    // Devuelve el código siguiente: siguiente(Articulo.class, "A", 3) → "A051"
    public String siguiente(Class<?> entidad, String prefijo, int digitos) {
        List<String> codigos = em.createQuery(
                        "SELECT e.codigo FROM " + entidad.getSimpleName() + " e WHERE e.codigo LIKE :prefijo",
                        String.class)
                .setParameter("prefijo", prefijo + "%")
                .getResultList();   // trae TODOS, también los inactivos

        int maximo = codigos.stream()
                .map(codigo -> codigo.substring(prefijo.length()))   // "A050" → "050"
                .filter(resto -> resto.matches("\\d+"))               // solo los que son número
                .mapToInt(Integer::parseInt)                          // "050" → 50
                .max()
                .orElse(0);                                           // si no hay ninguno, arranca de 0

        return prefijo + String.format("%0" + digitos + "d", maximo + 1);   // 51 → "A051"
    }
}