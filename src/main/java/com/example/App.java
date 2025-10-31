package com.example;

import static spark.Spark.*;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.*;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        int serverPort = 4567;
        try {
            String env = System.getenv("PORT");
            if (env != null && !env.isBlank()) serverPort = Integer.parseInt(env.trim());
        } catch (Exception ignored) {}
        port(serverPort);

        staticFiles.location("/public");

        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
        });
        options("/*", (req, res) -> { res.status(204); return ""; });

        Connection conn = Db.get();
        ProductDao productDao = new ProductDao(conn);

        if (productDao.findAll().isEmpty()) {
            Product a = new Product();
            a.setId("p1"); a.setName("Figura Goku");
            a.setDescr("SSJ Blue 15cm"); a.setImageUrl(null);
            a.setPrice(new BigDecimal("499.00")); a.setStock(10);
            productDao.create(a);

            Product b = new Product();
            b.setId("p2"); b.setName("Carta Pikachu");
            b.setDescr("Holo 1st ed"); b.setImageUrl(null);
            b.setPrice(new BigDecimal("1299.00")); b.setStock(5);
            productDao.create(b);
        }

        get("/ping", (req, res) -> "pong");

        path("/api", () -> {
            path("/products", () -> {
                get("", (req, res) -> json(res, productDao.findAll()));
                get("/:id", (req, res) -> productDao.findById(req.params("id"))
                        .<Object>map(p -> json(res, p))
                        .orElseGet(() -> err(res, 404, "Product not found")));
                post("/:id", (req, res) -> {
                    String id = req.params("id");
                    JsonObject body = parseJson(req);
                    String name  = str(body, "name");
                    String descr = str(body, "descr");
                    String image = str(body, "image_url");
                    BigDecimal price = decimal(body, "price");
                    Integer stock    = integer(body, "stock");

                    if (name == null || price == null || stock == null)
                        return err(res, 400, "Required: name, price, stock");
                    if (productDao.findById(id).isPresent())
                        return err(res, 409, "Product already exists");

                    Product p = new Product();
                    p.setId(id); p.setName(name); p.setDescr(descr);
                    p.setImageUrl(image); p.setPrice(price); p.setStock(stock);

                    productDao.create(p);
                    res.status(201);
                    return json(res, Map.of("id", id, "name", name));
                });
                put("/:id", (req, res) -> {
                    String id = req.params("id");
                    var opt = productDao.findById(id);
                    if (opt.isEmpty()) return err(res, 404, "Product not found");
                    Product p = opt.get();

                    JsonObject body = parseJson(req);
                    if (body.has("name"))      p.setName(str(body, "name"));
                    if (body.has("descr"))     p.setDescr(str(body, "descr"));
                    if (body.has("image_url")) p.setImageUrl(str(body, "image_url"));
                    if (body.has("price"))     p.setPrice(decimal(body, "price"));
                    if (body.has("stock"))     p.setStock(integer(body, "stock"));

                    productDao.update(p);
                    return json(res, p);
                });
                delete("/:id", (req, res) -> {
                    boolean ok = productDao.delete(req.params("id"));
                    if (!ok) return err(res, 404, "Product not found");
                    return json(res, Map.of("message", "Product deleted"));
                });
            });
        });

        log.info("API ready on port {}", serverPort);
    }

    private static JsonObject parseJson(spark.Request req) {
        try {
            return JsonParser.parseString(req.body()).getAsJsonObject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON body");
        }
    }

    private static Object json(spark.Response res, Object o) {
        res.type("application/json;charset=utf-8");
        return gson.toJson(o);
    }

    private static Object err(spark.Response res, int code, String msg) {
        res.status(code); res.type("application/json;charset=utf-8");
        return gson.toJson(Map.of("message", msg));
    }

    private static String str(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : null;
    }

    private static BigDecimal decimal(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? new BigDecimal(o.get(k).getAsString()) : null;
    }

    private static Integer integer(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsInt() : null;
    }
}
