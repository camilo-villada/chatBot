# LibroTech con ChatBot IA (Gemini + MongoDB + WebSockets)

Proyecto Spring Boot 4 que combina:

- **Modulo libros existente**: CRUD de libros con JPA + H2 y vistas Thymeleaf.
- **Modulo chatbot nuevo (Modulo 6.1 - Dia 4)**: chat en tiempo real con
  asistente IA, WebSockets STOMP, persistencia en MongoDB Atlas y respuestas
  generadas por **Gemini** (capa gratuita de Google AI Studio).

El chatbot aparece como un boton burbuja en la esquina inferior derecha
de TODAS las paginas del admin (`/admin/libros`, `/admin/libros/nuevo`...).

---

## Como arrancar

### 1. Variables a revisar en `src/main/resources/application.properties`

```properties
# MongoDB Atlas: el cluster es tuyo. La base "librotech_chat" la crea
# Mongo automaticamente al guardar el primer mensaje.
spring.data.mongodb.uri=mongodb+srv://adminchatbot:[email protected]/librotech_chat?retryWrites=true&w=majority&appName=riwi

# Gemini (Google AI Studio): genera tu API key gratuita en
#   https://aistudio.google.com/apikey
gemini.api.key=TU_API_KEY_AQUI
gemini.api.model=gemini-2.0-flash
```

> ⚠️ **IMPORTANTE**: Las credenciales que aparecen en este archivo son
> placeholders. Antes de subir el repo a GitHub:
>
> 1. Rota la contrasena del usuario `adminchatbot` en MongoDB Atlas
>    (Database Access -> Edit -> Edit Password).
> 2. Genera una API key nueva de Gemini y borra la antigua.
> 3. Considera mover ambos secretos a variables de entorno (`SPRING_DATA_MONGODB_URI`,
>    `GEMINI_API_KEY`) o a un fichero `application-local.properties` ignorado por git.

### 2. Permitir el acceso de red al cluster de Mongo

En MongoDB Atlas → **Network Access** → Add IP Address → **Allow Access from
Anywhere** (`0.0.0.0/0`) para pruebas, o agrega tu IP publica especificamente.

### 3. Compilar y ejecutar

```bash
mvn spring-boot:run
```

### 4. Probar

- Abre dos pestanas en http://localhost:8080/admin/libros
- Haz click en el boton burbuja 💬 abajo a la derecha.
- Ingresa tu nombre la primera vez.
- Escribe una pregunta (por ejemplo: "Recomiendame un libro de programacion").
- En la otra pestana veras tu mensaje en tiempo real y, segundos despues,
  la respuesta de LibroBot IA.
- Detén y reinicia el servidor: al recargar el chat, los mensajes siguen ahi
  (gracias a MongoDB).

### 5. Endpoints utiles

| Metodo | Ruta                       | Descripcion                                |
|--------|----------------------------|--------------------------------------------|
| GET    | /admin/libros              | UI con la lista de libros (lleva chatbot)  |
| GET    | /admin/libros/nuevo        | UI con el formulario (lleva chatbot)       |
| GET    | /api/libros                | API JSON de libros                         |
| GET    | /api/mensajes              | API JSON del historial del chat            |
| WS     | /chat-websocket            | Endpoint SockJS/STOMP del chat             |
| GET    | /h2-console                | Consola H2 (libros)                        |

---

## Arquitectura

```
com.riwi.librotech
├── LibrotechApplication.java        @EnableJpa + @EnableMongo
├── model/Libro.java                 Entidad JPA (H2)
├── Repository/LibroRepository.java  JpaRepository
├── Service/LibroService.java        Logica de libros
├── controller/
│   ├── LibroController.java         REST /api/libros
│   └── ui/LibroUIController.java    UI Thymeleaf /admin/libros
│
└── chat/                            NUEVO modulo chatbot
    ├── config/WebSocketConfig.java       STOMP /chat-websocket
    ├── model/Mensaje.java                Documento Mongo
    ├── repository/MensajeRepository.java MongoRepository
    ├── service/
    │   ├── MensajeService.java           Persistencia
    │   └── BotIAService.java             Llamada a Gemini
    └── controller/
        ├── MensajeRestController.java    REST /api/mensajes
        └── ws/ChatSocketController.java  @MessageMapping("/enviar")
```

### Flujo de un mensaje del chat

1. El usuario escribe en el widget → el JS envia STOMP a `/app/enviar`.
2. `ChatSocketController.procesarMensajeUsuario`:
   - guarda el mensaje en MongoDB,
   - lo difunde a `/tema/mensajes` (todos los suscritos lo ven),
   - en un **hilo aparte** invoca `BotIAService`.
3. `BotIAService`:
   - lee los ultimos N mensajes de Mongo (memoria/RAG),
   - construye un prompt y llama a **Gemini** via HTTP,
   - guarda la respuesta en Mongo,
   - se devuelve y `messagingTemplate.convertAndSend("/tema/mensajes", ...)`
     la difunde a los clientes.
4. Cada cliente actualiza su UI en tiempo real.

### Por que un hilo aparte para la IA

Si llamamos a Gemini de forma sincrona dentro del `@MessageMapping`, el hilo
del broker WebSocket queda **bloqueado durante varios segundos**. El propio
mensaje del usuario no se difundiria hasta que llegara la respuesta del bot,
y la sensacion en la UI seria de "lag". Sacar la llamada a otro hilo permite
que el mensaje del usuario aparezca al instante y la respuesta del bot vaya
llegando cuando este lista.

---

## Notas tecnicas

- **No usamos Spring AI** porque su conector oficial para Gemini requiere
  Vertex AI + credenciales de Google Cloud. La API REST de Google AI Studio
  ofrece capa gratuita y solo requiere una API key, asi que la consumimos
  directamente con `RestTemplate` + Jackson.
- **Mongo crea la base de datos al primer save**. No hace falta crear nada
  a mano en Atlas: solo el cluster (ya lo tienes).
- **JPA y Mongo conviven** porque declaramos explicitamente los paquetes de
  cada motor en la clase principal con `@EnableJpaRepositories` y
  `@EnableMongoRepositories`.
