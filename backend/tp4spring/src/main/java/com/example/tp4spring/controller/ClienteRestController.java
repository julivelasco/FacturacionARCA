package com.example.tp4spring.controller;

import com.example.tp4spring.dto.ClienteDTO;
import com.example.tp4spring.dto.ClienteRequestDTO;
import com.example.tp4spring.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClienteRestController {

    private final ClienteService clienteService;

    @GetMapping
    public Page<ClienteDTO> buscar (
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "false") boolean incluirBajas,
            @RequestParam(required = false) String orden,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {
        return clienteService.buscar(texto, incluirBajas, orden, pagina, tamanio);
    }

    @GetMapping("/{id}")
    public ClienteDTO obtener (@PathVariable Long id) {
        return clienteService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<ClienteDTO> crear (@RequestBody ClienteRequestDTO clienteDatos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(clienteDatos));
    }

    @PutMapping("/{id}")
    public ClienteDTO actualizar (@PathVariable Long id, @RequestBody ClienteRequestDTO clienteDatos) {
        return clienteService.actualizar(id, clienteDatos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja (@PathVariable Long id) {
        clienteService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactivar")
    public ClienteDTO reactivar (@PathVariable Long id) {
        return clienteService.reactivar(id);
    }
}
