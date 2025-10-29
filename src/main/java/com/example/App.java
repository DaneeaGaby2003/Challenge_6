package com.example;

import static spark.Spark.*;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final Gson gson = new Gson();

    // ---- EXCEPCIÓN API ----
    private static class ApiError extends Exception {
        private final int status;
        public ApiError(String message, int status) { super(message); this.status = status; }
        public int getStatus() { return this.status; }
    }

    public static void main(String[] args) {
        // Puerto configurable
        port(getPort());

        // CORS básico
        enableCORS();

        // Inicializa BD (H2) y DAO
        try { Db.init(); } catch (Exception e) { e.printStackTrace(); System.exit(1); }
        UserDao dao = new UserDao();

        // Manejador de errores
        exception(ApiError.class, (e, req, res) -> {
            res.status(e.getStatus());
            res.body(gson.toJson(jsonMsg(e.getMessage())));
            res.type("application/json");
        });

        // After → fuerza JSON (no toca strings como "OK" de CORS)
        after((req, res) -> {
            res.type("application/json");
            if (!(res.body() instanceof String)) {
                res.body(gson.toJson(res.body()));
            }
        });

        // ---- RUTAS ----
        get("/users", (req, res) -> dao.findAll());

        get("/users/:id", (req, res) -> {
            return findUserOr404(dao, req.params(":id"));
        });

        post("/users/:id", (req, res) -> {
            String id = req.params(":id");
            if (dao.exists(id)) throw new ApiError("User already exists", 409);

            User payload = gson.fromJson(req.body(), User.class); // <- com.example.User
            if (payload == null) throw new ApiError("Invalid body", 400);
            if (payload.getName() == null || payload.getEmail() == null)
                throw new ApiError("Invalid body. Required: name, email", 400);

            payload.setId(id);
            dao.insert(payload);
            res.status(201);
            return payload;
        });

        put("/users/:id", (req, res) -> {
            String id = req.params(":id");
            User existing = findUserOr404(dao, id);

            User payload = gson.fromJson(req.body(), User.class);
            if (payload == null) throw new ApiError("Invalid body", 400);

            if (payload.getName()  != null) existing.setName(payload.getName());
            if (payload.getEmail() != null) existing.setEmail(payload.getEmail());

            dao.update(existing);
            return existing;
        });

        options("/users/:id", (req, res) -> {
            res.header("Allow", "GET,POST,PUT,DELETE,OPTIONS");
            String id = req.params(":id");
            if (dao.exists(id)) { res.status(200); return jsonMsg("User exists"); }
            else { res.status(404); return jsonMsg("User does not exist"); }
        });

        delete("/users/:id", (req, res) -> {
            String id = req.params(":id");
            boolean removed = dao.delete(id);
            if (!removed) throw new ApiError("User not found", 404);
            return jsonMsg("User deleted");
        });

        // Semillas (solo si no existen)
        if (!dao.exists("1")) dao.insert(new User("1", "Rafael", "rafael@example.com"));
        if (!dao.exists("2")) dao.insert(new User("2", "Sofía", "sofia@example.com"));

        log.info("Spark Collectibles API (H2) running on port {}", getPort());
    }

    // ---- HELPERS ----
    private static User findUserOr404(UserDao dao, String id) throws ApiError {
        return dao.findById(id).orElseThrow(() -> new ApiError("User not found", 404));
    }

    private static int getPort() {
        String p = System.getenv("PORT");
        if (p == null) return 4567;
        try { return Integer.parseInt(p); } catch (NumberFormatException e) { return 4567; }
    }

    /** CORS mínimo para dev */
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
            return "OK"; // el 'after' no lo convierte a JSON
        });
    }

    private static Map<String, String> jsonMsg(String msg) {
        return Collections.singletonMap("message", msg);
    }
}
