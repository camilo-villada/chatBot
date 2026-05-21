package com.riwi.librotech.Repository;

import com.riwi.librotech.model.Libro;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {

    // CORRECCIÓN: el campo en Libro.java se llama "author", no "autor"
    // Spring Data JPA deriva el nombre del método del nombre real del campo
    List<Libro> findByAuthor(String author);

}
