package com.riwi.librotech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicacion principal.
 *
 * El proyecto convive con DOS motores de persistencia:
 *   - JPA + H2  → para la entidad Libro (modulo libros)
 *   - MongoDB   → para la coleccion Mensaje (modulo chatbot)
 *
 * Spring Boot detecta automaticamente cada repositorio por la interface
 * que extiende (JpaRepository -> JPA, MongoRepository -> Mongo), asi que
 * NO hace falta declarar @EnableJpaRepositories ni @EnableMongoRepositories
 * mientras los repos esten dentro del paquete base (com.riwi.librotech).
 */
@SpringBootApplication
public class LibrotechApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibrotechApplication.class, args);
	}

}
