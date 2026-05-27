package com.riwi.librotech.Repository;

import com.riwi.librotech.dto.LibroResumenDTO;
import com.riwi.librotech.model.Libro;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {

    @Override
    @EntityGraph(attributePaths = {"editorial", "generos"})
    Optional<Libro> findById(Long id);

    @EntityGraph(attributePaths = {"editorial", "generos"})
    @Query("select distinct l from Libro l")
    List<Libro> findAllWithRelaciones();

    @EntityGraph(attributePaths = {"editorial", "generos"})
    List<Libro> findByAutor(String autor);

    @Query("""
            select distinct l
            from Libro l
            left join fetch l.editorial
            left join fetch l.generos
            where l.id = :id
            """)
    Optional<Libro> findDetalleByIdJoinFetch(@Param("id") Long id);

    @Query("""
            select new com.riwi.librotech.dto.LibroResumenDTO(
                l.id,
                l.titulo,
                l.autor,
                e.nombre,
                l.fechaPublicacion,
                l.precio
            )
            from Libro l
            join l.editorial e
            """)
    Slice<LibroResumenDTO> findResumenes(Pageable pageable);

    @Query("""
            select new com.riwi.librotech.dto.LibroResumenDTO(
                l.id,
                l.titulo,
                l.autor,
                e.nombre,
                l.fechaPublicacion,
                l.precio
            )
            from Libro l
            join l.editorial e
            """)
    Page<LibroResumenDTO> findResumenesPage(Pageable pageable);

}
