package com.riwi.librotech.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.riwi.librotech.Repository.LibroRepository;
import com.riwi.librotech.dto.LibroResumenDTO;
import com.riwi.librotech.model.Libro;

@Service
public class LibroService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;
    private static final Set<String> SORT_FIELDS = Set.of("id", "titulo", "autor", "fechaPublicacion", "precio");

    @Autowired
    private LibroRepository libroRepository;

    public List<Libro> obtenerTodos() {
        return libroRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Libro> obtenerTodosConRelaciones() {
        return libroRepository.findAllWithRelaciones();
    }

    public Optional<Libro> obtenerPorId(Long id) {
        return libroRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Libro> obtenerPorIdConRelaciones(Long id) {
        return libroRepository.findDetalleByIdJoinFetch(id);
    }

    public Libro guardar(Libro libro) {
        return libroRepository.save(libro);
    }

    public Libro actualizar(long id, Libro datos) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado: " + id));

        libro.setTitulo(datos.getTitulo());
        libro.setAutor(datos.getAutor());
        libro.setIsbn(datos.getIsbn());
        libro.setFechaPublicacion(datos.getFechaPublicacion());
        libro.setPrecio(datos.getPrecio());
        libro.setEditorial(datos.getEditorial());
        if (datos.getGeneros() != null) libro.setGeneros(datos.getGeneros());

        return libroRepository.save(libro);
    }

    public Libro actualizarParcial(Long id, Libro cambios) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado: " + id));

        if (cambios.getTitulo() != null)         libro.setTitulo(cambios.getTitulo());
        if (cambios.getAutor() != null)          libro.setAutor(cambios.getAutor());
        if (cambios.getIsbn() != null)           libro.setIsbn(cambios.getIsbn());
        if (cambios.getFechaPublicacion() != null) libro.setFechaPublicacion(cambios.getFechaPublicacion());
        if (cambios.getPrecio() != null)          libro.setPrecio(cambios.getPrecio());
        if (cambios.getEditorial() != null)       libro.setEditorial(cambios.getEditorial());
        if (cambios.getGeneros() != null && !cambios.getGeneros().isEmpty()) libro.setGeneros(cambios.getGeneros());

        return libroRepository.save(libro);
    }

    public void eliminar(Long id) {
        descatalogarLibro(id);
    }

    @Transactional
    public Libro descatalogarLibro(Long id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado: " + id));

        libro.softDelete();
        return libroRepository.save(libro);
    }

    @Transactional(readOnly = true)
    public List<Libro> buscarPorAutor(String autor) {
        return libroRepository.findByAutor(autor);
    }

    @Transactional(readOnly = true)
    public Slice<LibroResumenDTO> obtenerResumenes(int page, int size, String sortBy, String direction) {
        return libroRepository.findResumenes(crearPageable(page, size, sortBy, direction));
    }

    @Transactional(readOnly = true)
    public Page<LibroResumenDTO> obtenerResumenesPage(int page, int size, String sortBy, String direction) {
        return libroRepository.findResumenesPage(crearPageable(page, size, sortBy, direction));
    }

    private Pageable crearPageable(int page, int size, String sortBy, String direction) {
        int safePage = Math.max(page, 0);
        int safeSize = normalizarTamano(size);
        String safeSortBy = SORT_FIELDS.contains(sortBy) ? sortBy : "id";
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.ASC);
        return PageRequest.of(safePage, safeSize, Sort.by(sortDirection, safeSortBy));
    }

    private int normalizarTamano(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
