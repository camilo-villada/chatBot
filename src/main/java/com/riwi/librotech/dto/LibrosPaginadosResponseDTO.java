package com.riwi.librotech.dto;

import java.util.List;

public record LibrosPaginadosResponseDTO(
        List<LibroResumenDTO> libros,
        int currentPage,
        int pageSize,
        boolean hasNext,
        boolean hasPrevious
) {
}
