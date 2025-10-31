package com.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    private final Connection c;

    public UserDao(Connection c) { this.c = c; }

    public List<User> findAll() {
        List<User> out = new ArrayList<>();
        String sql = "SELECT id,name,email FROM users ORDER BY id";
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new User(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email")));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }

    public Optional<User> findById(String id) {
        String sql = "SELECT id,name,email FROM users WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("email")));
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    /** Inserta nuevo usuario; lanza RuntimeException si hay PK duplicada. */
    public void create(User u) {
        String sql = "INSERT INTO users(id,name,email) VALUES(?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getId());
            ps.setString(2, u.getName());
            ps.setString(3, u.getEmail());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    /** Actualiza nombre/email del usuario existente (por id). */
    public void update(User u) {
        String sql = "UPDATE users SET name=?, email=? WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    /** Borra por id; devuelve true si borró, false si no existía. */
    public boolean delete(String id) {
        String sql = "DELETE FROM users WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
