package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Db {
    // Persistencia en archivo local: ./data/collectibles.mv.db
    private static final String URL  = "jdbc:h2:file:./data/collectibles;MODE=PostgreSQL;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASS = "";

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void init() throws SQLException {
        try (Connection c = get(); Statement st = c.createStatement()) {
            st.executeUpdate("""
        CREATE TABLE IF NOT EXISTS users(
          id    VARCHAR(64)  PRIMARY KEY,
          name  VARCHAR(200) NOT NULL,
          email VARCHAR(255) NOT NULL UNIQUE
        );
      """);
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);");
        }
    }
}
