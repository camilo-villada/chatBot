package com.riwi.librotech.controller;

import com.riwi.librotech.Service.LibroService;
import com.riwi.librotech.dto.LibrosPaginadosResponseDTO;
import com.riwi.librotech.dto.LibroResumenDTO;
import com.riwi.librotech.model.Libro;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST — devuelve JSON.
 * Ruta base: /api/libros
 *
 * ARQUITECTURA HÍBRIDA (Módulo 6.1):
 *   Este @RestController devuelve JSON para Postman / clientes externos.
 *   LibroUIController (@Controller) devuelve HTML para el navegador.
 *   Ambos comparten el mismo LibroService.
 */
@RestController
@RequestMapping("/api/libros")
public class LibroController {

    @Autowired
    private LibroService libroService;

    @GetMapping
    public ResponseEntity<?> listarLibros(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction) {
        if (page == null && size == null && sortBy == null && direction == null) {
            return ResponseEntity.ok(libroService.obtenerTodosConRelaciones());
        }

        Slice<LibroResumenDTO> slice = libroService.obtenerResumenes(
                page != null ? page : 0,
                size != null ? size : 20,
                sortBy != null ? sortBy : "id",
                direction != null ? direction : "asc"
        );

        LibrosPaginadosResponseDTO response = new LibrosPaginadosResponseDTO(
                slice.getContent(),
                slice.getNumber(),
                slice.getSize(),
                slice.hasNext(),
                slice.hasPrevious()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Libro> obtenerLibro(@PathVariable long id) {
        return libroService.obtenerPorIdConRelaciones(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/resumenes")
    public ResponseEntity<Slice<LibroResumenDTO>> listarResumenes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(libroService.obtenerResumenes(page, size, sortBy, direction));
    }

    @GetMapping("/resumenes/page")
    public ResponseEntity<Page<LibroResumenDTO>> listarResumenesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(libroService.obtenerResumenesPage(page, size, sortBy, direction));
    }

    @PostMapping
    public ResponseEntity<Libro> crearLibro(@RequestBody Libro libro) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libroService.guardar(libro));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Libro> actualizar(@PathVariable Long id, @RequestBody Libro libro) {
        return ResponseEntity.ok(libroService.actualizar(id, libro));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Libro> actualizarParcial(@PathVariable Long id, @RequestBody Libro cambios) {
        return ResponseEntity.ok(libroService.actualizarParcial(id, cambios));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLibro(@PathVariable Long id) {
        libroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Libro>> buscarPorAutor(@RequestParam String autor) {
        return ResponseEntity.ok(libroService.buscarPorAutor(autor));
    }
}
