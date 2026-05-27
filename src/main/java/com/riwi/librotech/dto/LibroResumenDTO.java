package com.riwi.librotech.dto;

import java.time.LocalDate;

public record LibroResumenDTO(
        Long id,
        String titulo,
        String autor,
        String editorial,
        LocalDate fechaPublicacion,
        Double precio
) {
}
