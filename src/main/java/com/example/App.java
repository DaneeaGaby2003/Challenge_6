package com.example;

import static spark.Spark.*;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// --- 1. Imports explícitos ---
import java.util.Collections;
import java.util.Map;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final Gson gson = new Gson();

    // ---- EXCEPCIÓN API ----
    private static class ApiError extends Exception {
        private final int status;
        public ApiError(String message, int status) {
            super(message);
            this.status = status;
        }
        public int getStatus() { return this.status; }
    }

    public static void main(String[] args) {
        // Puerto configurable
        port(getPort());

        // CORS básico
        enableCORS();

        // ---- Inicializa BD (H2) y DAO ----
        try {
            Db.init();
        } catch (Exception e) {
            // --- 2. Mejor log de errores ---
            log.error("¡Error fatal! No se pudo inicializar la base de datos.", e);
            System.exit(1);
        }
        UserDao dao = new UserDao();

        // ---- FILTROS Y EXCEPCIONES ----
        exception(ApiError.class, (e, req, res) -> {
            res.status(e.getStatus());
            res.body(gson.toJson(jsonMsg(e.getMessage())));
            res.type("application/json");
        });

        // Filtro "After": fuerza JSON
        after((req, res) -> {
            res.type("application/json");
            if (!(res.body() instanceof String)) {
                res.body(gson.toJson(res.body()));
            }
        });

        // ---- RUTAS (Endpoints) ----

        get("/users", (req, res) -> dao.findAll());

        // --- 3. Lambda más limpia (sin {} ni return) ---
        get("/users/:id", (req, res) ->
                findUserOr404(dao, req.params(":id"))
        );

        post("/users/:id", (req, res) -> {
            String id = req.params(":id");
            if (dao.exists(id)) {
                throw new ApiError("User already exists", 409); // 409 Conflict
            }

            User payload = gson.fromJson(req.body(), User.class);
            if (payload == null) throw new ApiError("Invalid body", 400);

            payload.setId(id);
            // --- 4. Validación movida al modelo ---
            if (!payload.isValidForCreate()) {
                throw new ApiError("Invalid body. Required: name, email (non-empty)", 400);
            }

            dao.insert(payload);
            res.status(201); // 201 Created
            return payload;
        });

        put("/users/:id", (req, res) -> {
            String id = req.params(":id");
            User existing = findUserOr404(dao, id);

            User payload = gson.fromJson(req.body(), User.class);
            if (payload == null) throw new ApiError("Invalid body", 400);

            // Solo actualiza campos enviados
            if (payload.getName() != null)  existing.setName(payload.getName());
            if (payload.getEmail() != null) existing.setEmail(payload.getEmail());

            dao.update(existing);
            return existing;
        });

        options("/users/:id", (req, res) -> {
            res.header("Allow", "GET,POST,PUT,DELETE,OPTIONS");
            String id = req.params(":id");
            if (dao.exists(id)) {
                res.status(200);
                return jsonMsg("User exists");
            } else {
                res.status(404);
                return jsonMsg("User does not exist");
            }
        });

        // --- 5. Ruta DELETE más RESTful ---
        delete("/users/:id", (req, res) -> {
            String id = req.params(":id");
            User user = findUserOr404(dao, id); // 1. Encuentra (o 404)
            dao.delete(id);                   // 2. Elimina
            return user;                      // 3. Devuelve el objeto eliminado
        });

        // Semillas de ejemplo (solo si no existen)
        if (!dao.exists("1")) dao.insert(new User("1", "Rafael", "rafael@example.com"));
        if (!dao.exists("2")) dao.insert(new User("2", "Sofía", "sofia@example.com"));

        log.info("Spark Collectibles API (H2) running on port {}", getPort());
    }

    // ---- MÉTODOS AUXILIARES ----

    private static User findUserOr404(UserDao dao, String id) throws ApiError {
        return dao.findById(id).orElseThrow(() -> new ApiError("User not found", 404));
    }

    private static int getPort() {
        String p = System.getenv("PORT");
        if (p == null) return 4567;
        try { return Integer.parseInt(p); } catch (NumberFormatException e) { return 4567; }
    }

    private static Map<String, String> jsonMsg(String msg) {
        return Collections.singletonMap("message", msg);
    }

    // ---- CLASE DE MODELO ----
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

        // --- 4. Método de validación ---
        /** Valida que los campos requeridos para crear estén presentes */
        public boolean isValidForCreate() {
            // isBlank() (Java 11+) comprueba null, "" y "   "
            return this.name != null && !this.name.isBlank() &&
                    this.email != null && !this.email.isBlank();
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