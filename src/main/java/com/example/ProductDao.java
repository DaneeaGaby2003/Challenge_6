package com.example;

import java.sql.*;
import java.util.*;
import java.math.BigDecimal;

public class ProductDao {
    private final Connection c;

    public ProductDao(Connection c) { this.c = c; }

    public List<Product> findAll() {
        List<Product> out = new ArrayList<>();
        String sql = "SELECT id,name,descr,image_url,price,stock FROM products ORDER BY id";
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }

    public Optional<Product> findById(String id) {
        String sql = "SELECT id,name,descr,image_url,price,stock FROM products WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    public void create(Product p) {
        String sql = "INSERT INTO products(id,name,descr,image_url,price,stock) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getDescr());
            ps.setString(4, p.getImageUrl());
            ps.setBigDecimal(5, p.getPrice());
            ps.setInt(6, p.getStock());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void update(Product p) {
        String sql = """
            UPDATE products
               SET name=?, descr=?, image_url=?, price=?, stock=?
             WHERE id=?
        """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescr());
            ps.setString(3, p.getImageUrl());
            ps.setBigDecimal(4, p.getPrice());
            ps.setInt(5, p.getStock());
            ps.setString(6, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    /** Devuelve true si borrÃ³, false si no existÃ­a. */
    public boolean delete(String id) {
        String sql = "DELETE FROM products WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getString("id"));
        p.setName(rs.getString("name"));
        p.setDescr(rs.getString("descr"));
        p.setImageUrl(rs.getString("image_url"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStock(rs.getInt("stock"));
        return p;
    }
}
