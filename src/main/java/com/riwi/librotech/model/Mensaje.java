package com.riwi.librotech.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Documento MongoDB que representa un mensaje del chat.
 *
 * @Document(collection = "mensajes")
 *   Spring Data Mongo guardara cada instancia en la coleccion "mensajes"
 *   dentro de la base de datos "librotech_chat" (configurada en
 *   application.properties).
 *
 * @Id  (de Spring Data, NO de JPA)
 *   Identificador unico generado por MongoDB. Es String porque Mongo usa
 *   ObjectId (24 caracteres hex), no enteros autoincrementales como JPA.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "mensajes")
public class Mensaje {

    @Id
    private String id;

    /** Quien envia: nombre del usuario o "LibroBot IA" para el bot. */
    private String remitente;

    /** Texto del mensaje. */
    private String contenido;

    /** Marca temporal: se autoasigna al construir un mensaje nuevo. */
    private LocalDateTime fechaEnvio = LocalDateTime.now();

    /**
     * Constructor de conveniencia para crear mensajes con solo
     * remitente y contenido (la fecha se autoasigna).
     */
    public Mensaje(String remitente, String contenido) {
        this.remitente = remitente;
        this.contenido = contenido;
        this.fechaEnvio = LocalDateTime.now();
    }
}
