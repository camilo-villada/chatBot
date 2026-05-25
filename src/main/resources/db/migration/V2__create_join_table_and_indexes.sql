-- V2: Tabla intermedia libros_generos + índices
-- Compatible con el @JoinTable(name = "libros_generos") en Libro

CREATE TABLE libros_generos (
    libro_id BIGINT NOT NULL,
    genero_id BIGINT NOT NULL,
    CONSTRAINT pk_libros_generos PRIMARY KEY (libro_id, genero_id),
    CONSTRAINT fk_libros_generos_libro
        FOREIGN KEY (libro_id)
        REFERENCES libros (id),
    CONSTRAINT fk_libros_generos_genero
        FOREIGN KEY (genero_id)
        REFERENCES generos (id)
);

-- Índices para rendimiento
CREATE INDEX idx_libros_editorial_id ON libros (editorial_id);
CREATE INDEX idx_libros_disponible ON libros (disponible);
CREATE INDEX idx_libros_fecha_publicacion ON libros (fecha_publicacion);

CREATE INDEX idx_libros_generos_genero_id ON libros_generos (genero_id);
CREATE INDEX idx_libros_generos_libro_id ON libros_generos (libro_id);
