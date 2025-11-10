package com.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para products + ofertas (tabla product_offers).
 * Usa H2 y MERGE para UPSERT.
 */
public class ProductDao {

    private final Connection conn;

    public ProductDao(Connection conn) {
        this.conn = conn;
        ensureOfferSchema();
    }

    /** Crea tabla de ofertas si no existe (no modifica Db.java). */
    private void ensureOfferSchema() {
        final String sql = """
        CREATE TABLE IF NOT EXISTS product_offers(
          product_id  VARCHAR(40) PRIMARY KEY,
          promo_price DECIMAL(12,2) NOT NULL,
          valid_until DATE NOT NULL,
          CONSTRAINT fk_offer_product FOREIGN KEY (product_id) REFERENCES products(id)
        )
        """;
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo asegurar el esquema de ofertas", e);
        }
    }

    /** Lista todos los productos, uniendo oferta si existe. */
    public List<Product> findAll() {
        final String sql = """
        SELECT p.id, p.name, p.descr, p.image_url, p.price, p.stock,
               o.promo_price, o.valid_until
        FROM products p
        LEFT JOIN product_offers o ON o.product_id = p.id
        ORDER BY p.name
        """;
        List<Product> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(mapRow(rs));
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando productos", e);
        }
    }

    /** Lista con filtro de texto en nombre/descr (case-insensitive). */
    public List<Product> findAllFiltered(String q) {
        String base = """
        SELECT p.id, p.name, p.descr, p.image_url, p.price, p.stock,
               o.promo_price, o.valid_until
        FROM products p
        LEFT JOIN product_offers o ON o.product_id = p.id
        """;
        boolean hasQ = q != null && !q.isBlank();
        String where = hasQ ? "WHERE LOWER(p.name) LIKE ? OR LOWER(p.descr) LIKE ?" : "";
        String order = " ORDER BY p.name";

        List<Product> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(base + where + order)) {
            if (hasQ) {
                String like = "%" + q.toLowerCase().trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapRow(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando productos con filtro", e);
        }
    }

    /** Busca por id y trae oferta si existe. */
    public Optional<Product> findById(String id) {
        final String sql = """
        SELECT p.id, p.name, p.descr, p.image_url, p.price, p.stock,
               o.promo_price, o.valid_until
        FROM products p
        LEFT JOIN product_offers o ON o.product_id = p.id
        WHERE p.id = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando producto " + id, e);
        }
    }

    /** Actualiza datos base del producto (name/descr/image/price/stock). */
    public void update(Product p) {
        final String sql = """
        UPDATE products
           SET name=?, descr=?, image_url=?, price=?, stock=?
         WHERE id=?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescr());
            ps.setString(3, p.getImageUrl());
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(p.getPrice()));
            ps.setInt(5, p.getStock());
            ps.setString(6, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando producto " + p.getId(), e);
        }
    }

    /** Crea/actualiza oferta usando MERGE (UPSERT) en H2. */
    public void saveOrUpdateOffer(String productId, double promoPrice, String validUntilIso) {
        final String sql = """
        MERGE INTO product_offers(product_id, promo_price, valid_until)
        KEY(product_id)
        VALUES (?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            ps.setBigDecimal(2, java.math.BigDecimal.valueOf(promoPrice));
            ps.setDate(3, java.sql.Date.valueOf(validUntilIso)); // yyyy-MM-dd
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando oferta para " + productId, e);
        }
    }

    // ===== util =====
    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getString("id"));
        p.setName(rs.getString("name"));
        p.setDescr(rs.getString("descr"));
        p.setImageUrl(rs.getString("image_url"));
        p.setPrice(rs.getBigDecimal("price").doubleValue());
        p.setStock(rs.getInt("stock"));

        java.math.BigDecimal promo = (java.math.BigDecimal) rs.getObject("promo_price");
        if (promo != null) p.setPromoPrice(promo.doubleValue());

        Date until = rs.getDate("valid_until");
        if (until != null) p.setValidUntil(until.toLocalDate().toString());

        return p;
    }
}
