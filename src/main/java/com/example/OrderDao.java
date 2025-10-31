package com.example;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.math.BigDecimal;

public class OrderDao {
    private final Connection c;

    public OrderDao(Connection c) { this.c = c; }

    public long create(String userId, BigDecimal total) {
        String sql = "INSERT INTO orders(user_id,total,created_at) VALUES(?,?,CURRENT_TIMESTAMP())";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, userId);
            ps.setBigDecimal(2, total);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            throw new RuntimeException("No se generó ID de orden");
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public Optional<Order> findById(long id) {
        String sql = "SELECT id,user_id,total,created_at FROM orders WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    private Order map(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime created = ts != null ? ts.toLocalDateTime() : null;
        return new Order(
                rs.getLong("id"),
                rs.getString("user_id"),
                rs.getBigDecimal("total"),
                created
        );
    }
}
