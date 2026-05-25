package com.riwi.librotech.controller.ui;

import com.riwi.librotech.Repository.EditorialRepository;
import com.riwi.librotech.Service.LibroService;
import com.riwi.librotech.model.Libro;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador de Interfaz de Usuario — Thymeleaf.
 * Ruta base: /admin/libros
 *
 * DIFERENCIA CLAVE con LibroController (REST):
 *   @Controller (este)   → return = nombre de PLANTILLA HTML
 *   @RestController      → return = cuerpo JSON de la respuesta
 *
 * Ambos usan el mismo LibroService → Arquitectura Híbrida (Módulo 6.1).
 */
@Controller
@RequestMapping("/admin/libros")
public class LibroUIController {

    private final LibroService libroService;
    private final EditorialRepository editorialRepository;

    public LibroUIController(LibroService libroService, EditorialRepository editorialRepository) {
        this.libroService = libroService;
        this.editorialRepository = editorialRepository;
    }

    // =========================================================================
    // LAB 1 — Listar libros con th:each
    // =========================================================================

    /**
     * GET /admin/libros
     *
     * model.addAttribute(nombre, valor):
     *   El "nombre" es la variable disponible en Thymeleaf como ${nombre}.
     *
     * return "libros/lista":
     *   Spring busca: src/main/resources/templates/libros/lista.html
     */
    @GetMapping
    public String listarLibrosUI(Model model) {
        List<Libro> libros = libroService.obtenerTodos();

        model.addAttribute("libros", libros);
        model.addAttribute("tituloPantalla", "Catálogo de Libros - Dashboard");
        model.addAttribute("totalLibros", libros.size());

        return "libros/lista";
    }

    // =========================================================================
    // LAB 2 — Mostrar formulario (GET /admin/libros/nuevo)
    // =========================================================================

    /**
     * Envía un Libro vacío al formulario para que th:object="${libro}" tenga
     * un objeto con el que hacer el binding de los campos.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCreacion(Model model) {
        model.addAttribute("libro", new Libro());
        model.addAttribute("editoriales", editorialRepository.findAll());
        model.addAttribute("tituloPantalla", "Registrar Nuevo Libro");
        return "libros/formulario";
    }

    // =========================================================================
    // LAB 2 + LAB 3 — Procesar formulario con PRG y validación
    // =========================================================================

    /**
     * POST /admin/libros/guardar
     *
     * @ModelAttribute("libro"):
     *   Spring lee cada campo del form (name="titulo", name="autor"…),
     *   busca el setter en Libro (generado por @Data de Lombok) y lo rellena.
     *   Ejemplo: name="titulo" → libro.setTitulo(valor)
     *
     * LAB 3 — Validación manual:
     *   Si el año supera el año actual → volvemos al formulario CON el objeto
     *   para que el usuario vea sus datos y el mensaje de error.
     *   NO usamos redirect aquí: redirect borraría los datos escritos.
     *
     * Patrón PRG (Lab 2):
     *   Si todo está bien → "redirect:/admin/libros"
     *   Spring emite HTTP 302 → el navegador hace GET → F5 no duplica libros.
     */
    @PostMapping("/guardar")
    public String guardarLibro(@ModelAttribute("libro") Libro libro, Model model) {

        // Validación de negocio: fecha de publicación no puede estar en el futuro
        if (libro.getFechaPublicacion() != null && libro.getFechaPublicacion().isAfter(LocalDate.now())) {
            model.addAttribute("errorAnio",
                "La fecha de publicación no puede ser posterior a hoy (" + LocalDate.now() + ").");
            model.addAttribute("tituloPantalla", "Registrar Nuevo Libro (Corrección)");
            model.addAttribute("editoriales", editorialRepository.findAll());

            // NO redirect: el objeto "libro" permanece en el Model con los datos
            // escritos → Thymeleaf los mostrará de nuevo en los inputs
            return "libros/formulario";
        }

        libroService.guardar(libro);

        // LAB 2 — Patrón PRG: redirect limpia el historial del navegador
        return "redirect:/admin/libros";
    }
}
