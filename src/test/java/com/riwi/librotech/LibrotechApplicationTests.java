package com.riwi.librotech;

import com.riwi.librotech.Repository.EditorialRepository;
import com.riwi.librotech.Repository.LibroRepository;
import com.riwi.librotech.model.Editorial;
import com.riwi.librotech.model.Libro;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class LibrotechApplicationTests {

	@Autowired
	private LibroRepository libroRepository;

	@Autowired
	private EditorialRepository editorialRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void deleteRepositoryAplicaSoftDeleteYFiltraLecturas() {
		Editorial editorial = editorialRepository.findById(1L)
				.orElseThrow(() -> new IllegalStateException("Editorial seed no encontrada"));
		jdbcTemplate.execute("ALTER TABLE libros ALTER COLUMN id RESTART WITH 1000");

		Libro libro = new Libro(
				"Prueba Soft Delete",
				"LibroTech QA",
				"SOFT-DELETE-001",
				LocalDate.of(2024, 1, 10),
				45000.0,
				editorial
		);

		Libro guardado = libroRepository.saveAndFlush(libro);
		Long id = guardado.getId();
		assertNotNull(id);

		libroRepository.delete(guardado);
		libroRepository.flush();

		Boolean disponible = jdbcTemplate.queryForObject(
				"SELECT disponible FROM libros WHERE id = ?",
				Boolean.class,
				id
		);

		assertEquals(Boolean.FALSE, disponible);

		Optional<Libro> resultado = libroRepository.findById(id);
		assertFalse(resultado.isPresent());
	}

}
