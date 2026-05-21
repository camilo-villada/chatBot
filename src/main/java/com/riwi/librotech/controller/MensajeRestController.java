package com.riwi.librotech.controller;

import com.riwi.librotech.model.Mensaje;
import com.riwi.librotech.Service.MensajeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST del chat — devuelve JSON.
 *
 * Util para:
 *   - Pintar el historial cuando el widget se abre por primera vez (sin
 *     depender de un controlador UI dedicado).
 *   - Consumir el historial desde otros clientes (Postman, herramientas de QA).
 *
 * Ruta base: /api/mensajes
 */
@RestController
@RequestMapping("/api/mensajes")
public class MensajeRestController {

    @Autowired
    private MensajeService mensajeService;

    /** GET /api/mensajes → historial completo en orden cronologico. */
    @GetMapping
    public ResponseEntity<List<Mensaje>> listarHistorial() {
        return ResponseEntity.ok(mensajeService.obtenerHistorial());
    }
}
