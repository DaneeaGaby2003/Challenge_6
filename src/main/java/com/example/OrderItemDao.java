package com.example;

import java.sql.*;
import java.util.*;
import java.math.BigDecimal;

public class OrderItemDao {
    private final Connection c;

    public OrderItemDao(Connection c) { this.c = c; }

    public void create(long orderId, String productId, int qty, BigDecimal price) {
        String sql = "INSERT INTO order_items(order_id,product_id,qty,price) VALUES(?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setString(2, productId);
            ps.setInt(3, qty);
            ps.setBigDecimal(4, price);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    /** Devuelve items con nombre de producto y subtotal precalculado para la vista */
    public List<OrderItem> findByOrder(long orderId) {
        String sql = """
            SELECT oi.id, oi.order_id, oi.product_id, oi.qty, oi.price,
                   p.name AS product_name
            FROM order_items oi
            JOIN products p ON p.id = oi.product_id
            WHERE oi.order_id = ?
            ORDER BY oi.id
        """;
        var out = new ArrayList<OrderItem>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem it = new OrderItem(
                            rs.getLong("id"),
                            rs.getLong("order_id"),
                            rs.getString("product_id"),
                            rs.getInt("qty"),
                            rs.getBigDecimal("price")
                    );
                    it.setProductName(rs.getString("product_name"));
                    it.setSubtotal(it.getPrice().multiply(new BigDecimal(it.getQty())));
                    out.add(it);
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }
}
