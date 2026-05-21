package com.riwi.librotech.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.riwi.librotech.model.Mensaje;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Collectors;

/**
 * Servicio del Bot — Genera respuestas inteligentes usando Gemini.
 *
 * ARQUITECTURA:
 *   En vez de Spring AI (cuyo conector oficial de Gemini exige Vertex AI +
 *   credenciales de GCP), llamamos directamente a la API REST publica de
 *   Google AI Studio. Esto:
 *     - Solo requiere una API key (capa gratuita).
 *     - No agrega dependencias extra; usa RestTemplate (Spring Web) y
 *       Jackson (incluido en Spring Boot por defecto).
 *
 * ENDPOINT:
 *   POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={key}
 *
 * BODY:
 *   { "contents": [ { "parts": [ { "text": "..." } ] } ] }
 *
 * PATRON RAG (Retrieval-Augmented Generation):
 *   1. RECUPERAMOS los ultimos N mensajes de MongoDB (memoria del chat).
 *   2. CONSTRUIMOS un prompt que combina ese historial con la pregunta nueva.
 *   3. GENERAMOS la respuesta llamando al modelo.
 *   4. GUARDAMOS la respuesta del bot en MongoDB para alimentar la siguiente
 *      iteracion.
 */
@Service
public class BotIAService {

    private static final Logger log = LoggerFactory.getLogger(BotIAService.class);

    private static final String REMITENTE_BOT = "LibroBot IA";

    @Autowired
    private MensajeService mensajeService;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model}")
    private String model;

    @Value("${gemini.api.url}")
    private String baseUrl;

    @Value("${gemini.contexto.mensajes:10}")
    private int cantidadContexto;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Punto de entrada: recibe la pregunta, genera una respuesta usando el
     * historial como contexto, la persiste y la devuelve.
     */
    public Mensaje generarRespuestaIA(String preguntaUsuario) {
        // 1. CONTEXTO: traer los ultimos N mensajes (orden cronologico).
        String historial = mensajeService.obtenerUltimos(cantidadContexto)
                .stream()
                .map(m -> m.getRemitente() + ": " + m.getContenido())
                .collect(Collectors.joining("\n"));

        // 2. PROMPT: instrucciones + memoria + nueva pregunta.
        String prompt = """
                Eres "LibroBot IA", el asistente virtual de LibroTech, un sistema de
                gestion de bibliotecas. Respondes siempre en espaniol, de forma breve,
                cordial y util. Si te preguntan por libros, autores, catalogo o sobre
                como usar la aplicacion, responde con base en lo que sepas del dominio
                bibliotecario. Si no sabes algo, dilo con honestidad.

                === HISTORIAL RECIENTE DEL CHAT (memoria) ===
                %s

                === NUEVA PREGUNTA DEL USUARIO ===
                %s

                Responde SOLO con el contenido del mensaje, sin prefijos como
                "LibroBot IA:" ni comillas.
                """.formatted(historial.isBlank() ? "(aun no hay historial)" : historial,
                              preguntaUsuario);

        // 3. LLAMADA al modelo.
        String textoRespuesta;
        try {
            textoRespuesta = llamarGemini(prompt);
        } catch (Exception e) {
            log.error("Error invocando a Gemini", e);
            textoRespuesta = "Lo siento, tuve un problema para responder en este momento. "
                           + "Intenta de nuevo en unos segundos.";
        }

        // 4. PERSISTIR la respuesta del bot.
        Mensaje mensajeBot = new Mensaje(REMITENTE_BOT, textoRespuesta);
        return mensajeService.guardarMensaje(mensajeBot);
    }

    /**
     * Llama a la API REST de Gemini y devuelve el texto de la primera
     * "candidate" generada.
     */
    private String llamarGemini(String prompt) throws Exception {
        String url = baseUrl + "/" + model + ":generateContent?key=" + apiKey;

        // Construir el JSON: { "contents": [ { "parts": [ { "text": "..." } ] } ] }
        ObjectNode root = mapper.createObjectNode();
        ArrayNode contents = root.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        ObjectNode part = parts.addObject();
        part.put("text", prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(root), headers);

        String responseBody = restTemplate.postForObject(url, request, String.class);

        // Parsear: candidates[0].content.parts[0].text
        JsonNode tree = mapper.readTree(responseBody);
        JsonNode candidates = tree.path("candidates");
        if (candidates.isMissingNode() || !candidates.isArray() || candidates.isEmpty()) {
            log.warn("Gemini devolvio sin candidates. Respuesta: {}", responseBody);
            return "No pude generar una respuesta en este momento.";
        }
        JsonNode firstText = candidates.get(0)
                .path("content")
                .path("parts");
        if (!firstText.isArray() || firstText.isEmpty()) {
            return "No pude generar una respuesta en este momento.";
        }
        return firstText.get(0).path("text").asText("").trim();
    }
}
