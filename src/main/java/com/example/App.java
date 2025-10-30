package com.example;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.SQLException;
import java.util.*;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final Gson gson = new Gson();

    // --- Excepción API JSON ---
    private static class ApiError extends Exception {
        private final int status;
        public ApiError(String message, int status) { super(message); this.status = status; }
        public int getStatus() { return status; }
    }

    public static void main(String[] args) {
        port(getPort());

        // Archivos estáticos ANTES de mapear rutas
        staticFiles.location("/public");

        enableCORS();

        // DB + DAO
        try {
            Db.init(); // puede lanzar SQLException
        } catch (SQLException e) {
            log.error("DB init failed", e);
            throw new RuntimeException(e);
        }
        UserDao dao = new UserDao();

        // Semillas usando Optional
        if (dao.findById("1").isEmpty()) dao.insert(new User("1","Rafael","rafael@example.com"));
        if (dao.findById("2").isEmpty()) dao.insert(new User("2","Sofía","sofia@example.com"));

        // ---------- EXCEPCIONES JSON ----------
        exception(ApiError.class, (e, req, res) -> {
            res.status(((ApiError)e).getStatus());
            res.type("application/json;charset=utf-8");
            res.body(gson.toJson(jsonMsg(e.getMessage())));
        });
        exception(JsonSyntaxException.class, (e, req, res) -> {
            res.status(400);
            res.type("application/json;charset=utf-8");
            res.body(gson.toJson(jsonMsg("Invalid JSON body")));
        });
        exception(Exception.class, (e, req, res) -> {
            res.status(500);
            res.type("application/json;charset=utf-8");
            res.body(gson.toJson(jsonMsg("Server error")));
            log.error("Unhandled error", e);
        });

        // ---------- API ----------
        get("/ping", (req, res) -> "pong");

        get("/users", (req, res) -> {
            res.type("application/json;charset=utf-8");
            return gson.toJson(dao.findAll());
        });

        get("/users/:id", (req, res) -> {
            res.type("application/json;charset=utf-8");
            String id = req.params(":id");
            Optional<User> u = dao.findById(id);
            if (u.isEmpty()) throw new ApiError("User not found", 404);
            return gson.toJson(u.get());
        });

        post("/users/:id", (req, res) -> {
            res.type("application/json;charset=utf-8");
            String id = req.params(":id");

            if (dao.findById(id).isPresent())
                throw new ApiError("User already exists", 409);

            User payload = gson.fromJson(req.body(), User.class);
            if (payload == null || payload.getName() == null || payload.getEmail() == null)
                throw new ApiError("Invalid body. Required: name, email", 400);

            payload.setId(id);
            dao.insert(payload);
            res.status(201);
            return gson.toJson(payload);
        });

        put("/users/:id", (req, res) -> {
            res.type("application/json;charset=utf-8");
            String id = req.params(":id");

            Optional<User> existingOpt = dao.findById(id);
            if (existingOpt.isEmpty()) throw new ApiError("User not found", 404);

            User existing = existingOpt.get();
            User payload = gson.fromJson(req.body(), User.class);
            if (payload == null) throw new ApiError("Invalid JSON body", 400);

            if (payload.getName() != null)  existing.setName(payload.getName());
            if (payload.getEmail() != null) existing.setEmail(payload.getEmail());

            dao.update(existing);
            return gson.toJson(existing);
        });

        delete("/users/:id", (req, res) -> {
            res.type("application/json;charset=utf-8");
            String id = req.params(":id");
            if (dao.findById(id).isEmpty()) throw new ApiError("User not found", 404);
            dao.delete(id);
            return gson.toJson(jsonMsg("User deleted"));
        });

        // ---------- VISTAS ----------
        MustacheTemplateEngine engine = new MustacheTemplateEngine();

        get("/", (req, res) -> {
            Map<String,Object> model = new HashMap<>();
            model.put("users", dao.findAll());
            return new ModelAndView(model, "users.mustache");
        }, engine);

        get("/users/new", (req, res) -> {
            Map<String,Object> model = new HashMap<>();
            model.put("mode", "create");
            model.put("isCreate", true);
            return new ModelAndView(model, "user_form.mustache");
        }, engine);

        get("/users/:id/edit", (req, res) -> {
            String id = req.params(":id");
            Optional<User> u = dao.findById(id);
            if (u.isEmpty()) halt(404, "Not found");
            Map<String,Object> model = new HashMap<>();
            model.put("mode", "edit");
            model.put("isEdit", true);
            model.put("user", u.get());
            return new ModelAndView(model, "user_form.mustache");
        }, engine);

        log.info("API ready on port {}", getPort());
    }

    private static int getPort() {
        String p = System.getenv("PORT");
        if (p == null) return 4567;
        try { return Integer.parseInt(p); } catch (NumberFormatException e) { return 4567; }
    }

    private static Map<String,String> jsonMsg(String msg){
        return Collections.singletonMap("message", msg);
    }

    private static void enableCORS(){
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
        });
        options("/*", (req, res) -> {
            String reqHeaders = req.headers("Access-Control-Request-Headers");
            if (reqHeaders != null) res.header("Access-Control-Allow-Headers", reqHeaders);
            String reqMethod = req.headers("Access-Control-Request-Method");
            if (reqMethod != null) res.header("Access-Control-Allow-Methods", reqMethod);
            res.type("application/json;charset=utf-8");
            return gson.toJson(jsonMsg("ok"));
        });
    }
}
