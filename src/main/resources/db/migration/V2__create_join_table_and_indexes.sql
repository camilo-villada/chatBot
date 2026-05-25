-- V2: Tabla intermedia Many-to-Many e indices de rendimiento

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

-- Indices para optimizar consultas frecuentes
CREATE INDEX idx_libros_editorial ON libros (editorial_id);
CREATE INDEX idx_libros_disponible ON libros (disponible);
CREATE INDEX idx_libros_fecha ON libros (fecha_publicacion);
CREATE INDEX idx_editoriales_pais ON editoriales (pais);
