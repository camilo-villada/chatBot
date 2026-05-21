package com.riwi.librotech.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        libro.setTittle(datos.getTittle());
        libro.setAuthor(datos.getAuthor());
        libro.setIsbn(datos.getIsbn());
        libro.setPublicationYear(datos.getPublicationYear());

        return libroRepository.save(libro);
    }

    public Libro actualizarParcial(Long id, Libro cambios) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado: " + id));

        if (cambios.getTittle() != null)         libro.setTittle(cambios.getTittle());
        if (cambios.getAuthor() != null)          libro.setAuthor(cambios.getAuthor());
        if (cambios.getIsbn() != null)           libro.setIsbn(cambios.getIsbn());
        if (cambios.getPublicationYear() != null) libro.setPublicationYear(cambios.getPublicationYear());

        return libroRepository.save(libro);
    }

    public void eliminar(Long id) {
        libroRepository.deleteById(id);
    }

    // CORRECCIÓN: usamos findByAuthor (campo real) en vez de findByAutor
    public List<Libro> buscarPorAutor(String author) {
        return libroRepository.findByAuthor(author);
    }
}
