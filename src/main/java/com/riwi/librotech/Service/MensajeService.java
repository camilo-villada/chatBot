package com.riwi.librotech.Service;

import com.riwi.librotech.model.Mensaje;
import com.riwi.librotech.Repository.MensajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Logica de negocio para mensajes del chat.
 *
 * Mantiene dos vistas del historial:
 *  - obtenerHistorial() → orden cronologico (para Thymeleaf en la UI)
 *  - obtenerUltimos(n)  → orden cronologico de los N mas recientes (para Gemini)
 */
@Service
public class MensajeService {

    @Autowired
    private MensajeRepository mensajeRepository;

    /** Guarda un mensaje. Si no trae fecha, le asigna la actual. */
    public Mensaje guardarMensaje(Mensaje mensaje) {
        if (mensaje.getFechaEnvio() == null) {
            mensaje.setFechaEnvio(LocalDateTime.now());
        }
        return mensajeRepository.save(mensaje);
    }

    /** Historial COMPLETO, del mas antiguo al mas nuevo. */
    public List<Mensaje> obtenerHistorial() {
        return mensajeRepository.findAllByOrderByFechaEnvioAsc();
    }

    /**
     * Trae los ultimos N mensajes en orden CRONOLOGICO ascendente.
     * Internamente pide a Mongo "los N mas recientes" (desc) y luego invierte
     * la lista para presentarsela a Gemini en el orden en que sucedieron.
     */
    public List<Mensaje> obtenerUltimos(int n) {
        List<Mensaje> recientesDesc = mensajeRepository
                .findAllByOrderByFechaEnvioDesc(PageRequest.of(0, n));
        Collections.reverse(recientesDesc); // ahora estan en orden cronologico
        return recientesDesc;
    }
}
