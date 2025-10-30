package com.example;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final Gson gson = new Gson();

    // ---- Error JSON controlado
    private static class ApiError extends Exception {
        private final int status;
        ApiError(String msg, int status){ super(msg); this.status=status; }
        int getStatus(){ return status; }
    }

    public static void main(String[] args) {
        // 1) Arranque básico
        port(getPort());
        enableCORS();

        // 2) (Opcional) inicializa H2 y DAO
        try { Db.init(); } catch (Exception e) {
            log.error("DB init error", e);
            System.exit(1);
        }
        UserDao dao = new UserDao();

        // 3) Excepciones en JSON
        exception(ApiError.class, (e,req,res) -> {
            res.status(e.getStatus());
            res.type("application/json; charset=utf-8");
            res.body(gson.toJson(msg(e.getMessage())));
        });
        exception(JsonSyntaxException.class, (e,req,res) -> {
            res.status(400);
            res.type("application/json; charset=utf-8");
            res.body(gson.toJson(msg("Invalid JSON body")));
        });
        exception(Exception.class, (e,req,res) -> {
            log.error("Unhandled", e);
            res.status(500);
            res.type("application/json; charset=utf-8");
            res.body(gson.toJson(msg("Internal error")));
        });

        // 4) Rutas (solo API)
        get("/ping", (req,res) -> "pong");

        get("/users", (req,res) -> {
            res.type("application/json; charset=utf-8");
            return dao.findAll();
        }, gson::toJson);

        get("/users/:id", (req,res) -> {
            res.type("application/json; charset=utf-8");
            return dao.findById(req.params(":id"))
                    .orElseThrow(() -> new ApiError("User not found",404));
        }, gson::toJson);

        post("/users/:id", (req,res) -> {
            res.type("application/json; charset=utf-8");
            String id = req.params(":id");
            if (id==null || id.isBlank()) throw new ApiError("Invalid id",400);
            if (dao.exists(id))           throw new ApiError("User already exists",409);
            User body = gson.fromJson(req.body(), User.class);
            if (body==null || body.getName()==null || body.getEmail()==null)
                throw new ApiError("Invalid body. Required: name, email",400);
            body.setId(id);
            dao.insert(body);
            res.status(201);
            return body;
        }, gson::toJson);

        put("/users/:id", (req,res) -> {
            res.type("application/json; charset=utf-8");
            String id = req.params(":id");
            User u = dao.findById(id).orElseThrow(() -> new ApiError("User not found",404));
            User body = gson.fromJson(req.body(), User.class);
            if (body==null) throw new ApiError("Invalid body",400);
            if (body.getName()!=null)  u.setName(body.getName());
            if (body.getEmail()!=null) u.setEmail(body.getEmail());
            dao.update(u);
            return u;
        }, gson::toJson);

        delete("/users/:id", (req,res) -> {
            res.type("application/json; charset=utf-8");
            if (!dao.delete(req.params(":id"))) throw new ApiError("User not found",404);
            return msg("User deleted");
        }, gson::toJson);

        // 5) Semillas
        if (!dao.exists("1")) dao.insert(new User("1","Rafael","rafael@example.com"));
        if (!dao.exists("2")) dao.insert(new User("2","Sofía","sofia@example.com"));

        log.info("API ready on port {}", getPort());
    }

    // ---- Utiles
    private static int getPort(){
        try { return Integer.parseInt(System.getenv().getOrDefault("PORT","4567")); }
        catch(Exception e){ return 4567; }
    }
    private static Map<String,String> msg(String m){ return Collections.singletonMap("message",m); }

    private static void enableCORS() {
        before((rq,rs)->{
            rs.header("Access-Control-Allow-Origin","*");
            rs.header("Access-Control-Allow-Methods","GET,POST,PUT,DELETE,OPTIONS");
            rs.header("Access-Control-Allow-Headers","Content-Type,Authorization");
        });
        options("/*",(rq,rs)->{
            String h=rq.headers("Access-Control-Request-Headers");
            if(h!=null) rs.header("Access-Control-Allow-Headers",h);
            String m=rq.headers("Access-Control-Request-Method");
            if(m!=null) rs.header("Access-Control-Allow-Methods",m);
            return "OK";
        });
    }
}
