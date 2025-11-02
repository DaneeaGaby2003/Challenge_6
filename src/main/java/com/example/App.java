package com.example;

import static spark.Spark.*;

import java.sql.Connection;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        // Archivos estáticos (src/main/resources/public)
        staticFiles.location("/public");

        // Handlers de errores/excepciones
        exception(IllegalArgumentException.class, (ex, req, res) -> {
            log.warn("Solicitud inválida: {}", ex.getMessage());
            res.status(400);
            res.body("Solicitud inválida: " + ex.getMessage());
        });

        notFound((req, res) -> {
            res.type("text/html; charset=utf-8");
            return "<h1>404</h1><p>Recurso no encontrado.</p>";
        });

        internalServerError((req, res) -> {
            res.type("text/html; charset=utf-8");
            return "<h1>500</h1><p>Error interno. Intenta más tarde.</p>";
        });

        // Ruta de diagnóstico
        get("/ping", (req, res) -> "pong");

        // ===== Conexión H2 y DAO =====
        Connection conn = Db.get();
        ProductDao productDao = new ProductDao(conn);

        // ===== Ruta Home (index) con filtro 'q' =====
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("year", Calendar.getInstance().get(Calendar.YEAR));

            // Mensaje flash
            String flash = req.session().attribute("flash");
            if (flash != null) {
                model.put("flash", flash);
                req.session().removeAttribute("flash");
            }

            // Filtro
            String q = req.queryParams("q");
            model.put("q", q == null ? "" : q);

            try {
                List<Product> products = (q == null || q.isBlank())
                        ? productDao.findAll()
                        : productDao.findAllFiltered(q);
                model.put("products", products);
            } catch (Exception e) {
                log.error("Error consultando productos", e);
                model.put("products", java.util.List.of());
                model.put("error", "No se pudieron cargar los productos.");
            }

            return new ModelAndView(model, "index.mustache");
        }, new MustacheTemplateEngine());

        // ===== Formulario de ofertas =====
        post("/offers", (req, res) -> {
            try {
                String itemId        = value(req.queryParams("itemId"));
                String promoPriceStr = value(req.queryParams("promoPrice"));
                String validUntil    = value(req.queryParams("validUntil")); // yyyy-MM-dd

                if (itemId.isBlank() || promoPriceStr.isBlank() || validUntil.isBlank()) {
                    throw new IllegalArgumentException("Todos los campos son obligatorios.");
                }

                double promoPrice;
                try {
                    promoPrice = Double.parseDouble(promoPriceStr);
                    if (promoPrice < 0) throw new NumberFormatException();
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("Precio promocional inválido.");
                }

                // Verifica que el producto exista
                Product p = productDao.findById(itemId)
                        .orElseThrow(() -> new IllegalArgumentException("El producto no existe: " + itemId));

                // Guarda/actualiza oferta
                productDao.saveOrUpdateOffer(itemId, promoPrice, validUntil);

                req.session().attribute("flash", "Oferta guardada para " + p.getName());
                res.redirect("/");
                return null;

            } catch (IllegalArgumentException ex) {
                res.status(400);
                return "Error: " + ex.getMessage();
            } catch (Exception e) {
                log.error("Error al guardar la oferta", e);
                res.status(500);
                return "Error interno al guardar la oferta.";
            }
        });
    }

    private static String value(String s) {
        return (s == null) ? "" : s.trim();
    }
}
