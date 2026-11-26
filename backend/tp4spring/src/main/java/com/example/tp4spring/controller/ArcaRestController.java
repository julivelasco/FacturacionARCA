package com.example.tp4spring.controller;

import com.example.tp4spring.arca.ArcaAutenticacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/arca")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ArcaRestController {

    private final ArcaAutenticacionService arcaAutenticacionService;

    @GetMapping("/estado")
    public Map<String, Object> estado() {
        ArcaAutenticacionService.TicketAcceso ticket = arcaAutenticacionService.obtenerTicket();
        return Map.of("conectado", true, "paseVence", ticket.vencimiento().toString());
    }
}