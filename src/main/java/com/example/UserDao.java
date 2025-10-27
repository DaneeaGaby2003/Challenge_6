package com.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

    public List<User> findAll() {
        String sql = "SELECT id,name,email FROM users ORDER BY id";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<User> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public Optional<User> findById(String id) {
        String sql = "SELECT id,name,email FROM users WHERE id=?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public boolean exists(String id) {
        String sql = "SELECT 1 FROM users WHERE id=?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void insert(User u) {
        String sql = "INSERT INTO users(id,name,email) VALUES (?,?,?)";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getId());
            ps.setString(2, u.getName());
            ps.setString(3, u.getEmail());
            ps.executeUpdate();
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Unique index or primary key violation")) {
                throw new RuntimeException("User already exists or email already used", e);
            }
            throw new RuntimeException(e);
        }
    }

    public void update(User u) {
        String sql = "UPDATE users SET name=?, email=? WHERE id=?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public boolean delete(String id) {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("email")
        );
    }
}
