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

    public static void main(String[] args) {
        // Puerto configurable (default 4567)
        port(getPort());

        // CORS básico
        enableCORS();

        // Rutas: verbo + ruta + callback (Spark docs)
        get("/users", (req, res) -> {
            res.type("application/json");
            return gson.toJson(USERS.values());
        });

        get("/users/:id", (req, res) -> {
            res.type("application/json");
            String id = req.params(":id");
            User u = USERS.get(id);
            if (u == null) {
                res.status(404);
                return jsonMsg("User not found");
            }
            return gson.toJson(u);
        });

        post("/users/:id", (req, res) -> {
            res.type("application/json");
            String id = req.params(":id");
            if (USERS.containsKey(id)) {
                res.status(409);
                return jsonMsg("User already exists");
            }
            User payload = gson.fromJson(req.body(), User.class);
            if (payload == null) payload = new User();
            payload.setId(id);
            if (payload.getName() == null || payload.getEmail() == null) {
                res.status(400);
                return jsonMsg("Invalid body. Required: name, email");
            }
            USERS.put(id, payload);
            res.status(201);
            return gson.toJson(payload);
        });

        put("/users/:id", (req, res) -> {
            res.type("application/json");
            String id = req.params(":id");
            User existing = USERS.get(id);
            if (existing == null) {
                res.status(404);
                return jsonMsg("User not found");
            }
            User payload = gson.fromJson(req.body(), User.class);
            if (payload == null) {
                res.status(400);
                return jsonMsg("Invalid body");
            }
            // Solo actualiza campos enviados
            if (payload.getName() != null) existing.setName(payload.getName());
            if (payload.getEmail() != null) existing.setEmail(payload.getEmail());
            USERS.put(id, existing);
            return gson.toJson(existing);
        });

        options("/users/:id", (req, res) -> {
            res.type("application/json");
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
            res.type("application/json");
            String id = req.params(":id");
            User removed = USERS.remove(id);
            if (removed == null) {
                res.status(404);
                return jsonMsg("User not found");
            }
            return gson.toJson(removed);
        });

        // Semillas de ejemplo (opcional)
        USERS.put("1", new User("1", "Rafael", "rafael@example.com"));
        USERS.put("2", new User("2", "Sofía", "sofia@example.com"));

        log.info("Spark Collectibles API running on port {}", getPort());
    }

    private static int getPort() {
        String p = System.getenv("PORT");
        if (p == null) return 4567;
        try { return Integer.parseInt(p); } catch (NumberFormatException e) { return 4567; }
    }

    private static String jsonMsg(String msg) {
        return gson.toJson(Collections.singletonMap("message", msg));
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
            return "OK";
        });
    }
}