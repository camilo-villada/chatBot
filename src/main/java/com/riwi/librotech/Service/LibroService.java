package com.riwi.librotech.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.riwi.librotech.Repository.LibroRepository;
import com.riwi.librotech.model.Libro;

@Service
public class LibroService {

    @Autowired
    private LibroRepository libroRepository;

    public List<Libro> obtenerTodos() {
        return libroRepository.findAll();
    }

    public Optional<Libro> obtenerPorId(Long id) {
        return libroRepository.findById(id);
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

    public List<Libro> buscarPorAutor(String autor) {
        return libroRepository.findByAutor(autor);
    }
}
