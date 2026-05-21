package com.riwi.librotech.Repository;

import com.riwi.librotech.model.Mensaje;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio MongoDB del chat.
 *
 * Al extender MongoRepository<Mensaje, String>:
 *   - Spring Data genera la implementacion en runtime.
 *   - El segundo generico (String) es el tipo del @Id.
 *
 * Metodos derivados:
 *   findAllByOrderByFechaEnvioAsc()  → todos los mensajes en orden cronologico
 *   findAllByOrderByFechaEnvioDesc(Pageable) → los N mas recientes para la IA
 */
@Repository
public interface MensajeRepository extends MongoRepository<Mensaje, String> {

    /**
     * Trae el historial COMPLETO ordenado del mas antiguo al mas nuevo.
     * Util para renderizar la sala de chat con Thymeleaf.
     */
    List<Mensaje> findAllByOrderByFechaEnvioAsc();

    /**
     * Trae los N mensajes mas recientes (orden descendente).
     * Util para construir el contexto que pasaremos a Gemini.
     * Ej: usar PageRequest.of(0, 10) para los ultimos 10.
     */
    List<Mensaje> findAllByOrderByFechaEnvioDesc(Pageable pageable);
}
