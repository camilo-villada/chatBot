package com.riwi.librotech.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.riwi.librotech.Repository.EditorialRepository;
import com.riwi.librotech.model.Editorial;

@Component
public class StringToEditorialConverter implements Converter<String, Editorial> {

    private final EditorialRepository editorialRepository;

    public StringToEditorialConverter(EditorialRepository editorialRepository) {
        this.editorialRepository = editorialRepository;
    }

    @Override
    public Editorial convert(String source) {
        if (source == null) {
            return null;
        }

        String trimmed = source.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        Long id;
        try {
            id = Long.valueOf(trimmed);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Editorial id inválido: '" + source + "'", ex);
        }

        return editorialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Editorial no encontrada: " + id));
    }
}
