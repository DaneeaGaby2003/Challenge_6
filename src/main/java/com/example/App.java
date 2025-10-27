package com.example;

import static spark.Spark.*;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final Gson gson = new Gson();

    // "DB" en memoria
    private static final Map<String, User> USERS = new ConcurrentHashMap<>();

    // --- CORRECCIÓN 1: ApiError AHORA ES UNA EXCEPCIÓN ---
    // Clase de error personalizada que hereda de Exception
    private static class ApiError extends Exception {
        private final int status;

        public ApiError(String message, int status) {
            super(message); // Pasa el mensaje al constructor de Exception
            this.status = status;
        }

        public int getStatus() {
            return this.status;
        }
    }

    public static void main(String[] args) {
        // Puerto configurable
        port(getPort());

        // CORS básico
        enableCORS();

        // ---- FILTROS Y EXCEPCIONES ----

        // --- CORRECCIÓN 2: El manejador ahora usa getStatus() y getMessage() ---
        exception(ApiError.class, (e, req, res) -> {
            res.status(e.getStatus()); // Usa el getter para el estado
            res.body(gson.toJson(jsonMsg(e.getMessage()))); // Usa getMessage() de la Excepción
            res.type("application/json");
        });

        // Filtro "After": Convierte automáticamente la respuesta a JSON
        after((req, res) -> {
            res.type("application/json");
            // No convertimos si el body ya es un String (como en los "OK" de CORS)
            if (!(res.body() instanceof String)) {
                res.body(gson.toJson(res.body()));
            }
        });

        // ---- RUTAS (Endpoints) ----

        get("/users", (req, res) -> {
            return USERS.values();
        });

        get("/users/:id", (req, res) -> {
            // Usamos el helper que lanza error si no encuentra
            return findUserById(req.params(":id"));
        });

        post("/users/:id", (req, res) -> {
            String id = req.params(":id");
            if (USERS.containsKey(id)) {
                throw new ApiError("User already exists", 409); // 409 Conflict
            }

            User payload = gson.fromJson(req.body(), User.class);
            if (payload == null) {
                throw new ApiError("Invalid body", 400); // 400 Bad Request
            }

            payload.setId(id);
            if (payload.getName() == null || payload.getEmail() == null) {
                throw new ApiError("Invalid body. Required: name, email", 400);
            }

            USERS.put(id, payload);
            res.status(201); // 201 Created
            return payload;
        });

        put("/users/:id", (req, res) -> {
            String id = req.params(":id");
            User existing = findUserById(id); // Helper ya maneja el 404

            User payload = gson.fromJson(req.body(), User.class);
            if (payload == null) {
                throw new ApiError("Invalid body", 400);
            }

            // Solo actualiza campos enviados
            if (payload.getName() != null) existing.setName(payload.getName());
            if (payload.getEmail() != null) existing.setEmail(payload.getEmail());
            USERS.put(id, existing);
            return existing;
        });

        options("/users/:id", (req, res) -> {
            res.header("Allow", "GET,POST,PUT,DELETE,OPTIONS");
            String id = req.params(":id");
            if (USERS.containsKey(id)) {
                res.status(200);
                return jsonMsg("User exists");
            } else {
                res.status(404);
                return jsonMsg("User does not exist");
            }
        });

        delete("/users/:id", (req, res) -> {
            String id = req.params(":id");
            User removed = USERS.remove(id);
            if (removed == null) {
                throw new ApiError("User not found", 404);
            }
            return removed;
        });

        // Semillas de ejemplo
        USERS.put("1", new User("1", "Rafael", "rafael@example.com"));
        USERS.put("2", new User("2", "Sofía", "sofia@example.com"));

        log.info("Spark Collectibles API running on port {}", getPort());
    }

    // ---- MÉTODOS AUXILIARES ----

    /**
     * Busca un usuario por ID o lanza una ApiError(404) si no se encuentra.
     */
    private static User findUserById(String id) throws ApiError {
        User u = USERS.get(id);
        if (u == null) {
            throw new ApiError("User not found", 404);
        }
        return u;
    }

    private static int getPort() {
        String p = System.getenv("PORT");
        if (p == null) return 4567;
        try {
            return Integer.parseInt(p);
        } catch (NumberFormatException e) {
            return 4567;
        }
    }

    /**
     * Helper para crear un mensaje simple. Devuelve un Map.
     */
    private static Map<String, String> jsonMsg(String msg) {
        return Collections.singletonMap("message", msg);
    }

    // CORS mínimo para dev
    private static void enableCORS() {
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
        });
        options("/*", (request, response) -> {
            String reqHeaders = request.headers("Access-Control-Request-Headers");
            if (reqHeaders != null) response.header("Access-Control-Allow-Headers", reqHeaders);
            String reqMethod = request.headers("Access-Control-Request-Method");
            if (reqMethod != null) response.header("Access-Control-Allow-Methods", reqMethod);
            return "OK"; // El filtro 'after' NO lo convertirá a JSON
        });
    }

    // ---- CLASE DE MODELO (Ejemplo) ----
    public static class User {
        private String id;
        private String name;
        private String email;

        public User() {}

        public User(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        // --- Getters y Setters ---
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}