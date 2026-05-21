package com.riwi.librotech.controller.ws;

import com.riwi.librotech.model.Mensaje;
import com.riwi.librotech.Service.BotIAService;
import com.riwi.librotech.Service.MensajeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Controlador en tiempo real del chat.
 *
 * FLUJO de un mensaje:
 *  1. El cliente envia a /app/enviar (STOMP).
 *  2. Spring invoca procesarMensajeUsuario(Mensaje).
 *  3. Guardamos el mensaje del usuario en MongoDB.
 *  4. Lo retornamos (gracias a @SendTo, se difunde a /tema/mensajes → todos los
 *     clientes suscritos lo ven instantaneamente).
 *  5. En un hilo aparte (para no bloquear el WS) le pedimos a Gemini que
 *     genere su respuesta y la difundimos a /tema/mensajes con
 *     SimpMessagingTemplate.
 */
@Controller
public class ChatSocketController {

    private static final Logger log = LoggerFactory.getLogger(ChatSocketController.class);

    @Autowired
    private MensajeService mensajeService;

    @Autowired
    private BotIAService botIAService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/enviar")
    @SendTo("/tema/mensajes")
    public Mensaje procesarMensajeUsuario(Mensaje mensajeRecibido) {
        // 1. Persistimos en MongoDB.
        Mensaje mensajeGuardado = mensajeService.guardarMensaje(mensajeRecibido);
        log.info("Usuario [{}] dijo: {}", mensajeGuardado.getRemitente(), mensajeGuardado.getContenido());

        // 2. Disparamos la respuesta de la IA en segundo plano.
        //    (Si lo hicieramos sincrono, el hilo del WS quedaria bloqueado
        //    durante varios segundos esperando a Gemini y el usuario no veria
        //    su propio mensaje hasta entonces.)
        new Thread(() -> {
            try {
                Mensaje respuestaIA = botIAService.generarRespuestaIA(mensajeGuardado.getContenido());
                messagingTemplate.convertAndSend("/tema/mensajes", respuestaIA);
            } catch (Exception e) {
                log.error("Fallo generando respuesta del bot", e);
            }
        }, "bot-ia-worker").start();

        // 3. Devolvemos el mensaje del usuario para difusion inmediata.
        return mensajeGuardado;
    }
}
