package com.example;

import org.junit.jupiter.api.*;
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductDaoTest {

    Connection conn;
    ProductDao dao;

    @BeforeEach
    void setup() throws Exception {
        Class.forName("org.h2.Driver");
        // BD H2 ÚNICA por test (nombre aleatorio)
        String db = "t" + System.nanoTime();
        conn = DriverManager.getConnection("jdbc:h2:mem:" + db + ";MODE=PostgreSQL", "sa", "");

        // 1) Crear tabla products
        try (Statement st = conn.createStatement()) {
            st.execute("""
        CREATE TABLE products(
          id        VARCHAR(40) PRIMARY KEY,
          name      VARCHAR(120) NOT NULL,
          descr     VARCHAR(2000),
          image_url VARCHAR(500),
          price     DECIMAL(12,2) NOT NULL,
          stock     INT NOT NULL
        )
      """);
        }

        // 2) Seed
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO products(id,name,descr,image_url,price,stock) VALUES (?,?,?,?,?,?)")) {
            ps.setString(1, "p1"); ps.setString(2, "Figura Goku"); ps.setString(3, "SSJ Blue 15cm");
            ps.setString(4, null); ps.setBigDecimal(5, new BigDecimal("499.00")); ps.setInt(6, 10);
            ps.addBatch();

            ps.setString(1, "p2"); ps.setString(2, "Carta Pikachu"); ps.setString(3, "Holo 1st ed");
            ps.setString(4, null); ps.setBigDecimal(5, new BigDecimal("1299.00")); ps.setInt(6, 5);
            ps.addBatch();

            ps.executeBatch();
        }

        // 3) Instanciar DAO (crea product_offers si no existe)
        dao = new ProductDao(conn);
    }

    @AfterEach
    void tearDown() throws Exception {
        conn.close();
    }

    @Test
    void findAll_regresa_productos_ordenados_por_nombre() {
        List<Product> all = dao.findAll();
        assertEquals(2, all.size());
        assertEquals("Carta Pikachu", all.get(0).getName());
        assertEquals("Figura Goku", all.get(1).getName());
    }

    @Test
    void findAllFiltered_funciona_con_query_y_con_nulo_o_blanco() {
        List<Product> goku = dao.findAllFiltered("gOkU");
        assertEquals(1, goku.size());
        assertEquals("p1", goku.get(0).getId());

        assertEquals(2, dao.findAllFiltered(null).size());
        assertEquals(2, dao.findAllFiltered("   ").size());
    }

    @Test
    void findById_encuentra_y_no_encuentra() {
        Optional<Product> p1 = dao.findById("p1");
        assertTrue(p1.isPresent());
        assertEquals("Figura Goku", p1.get().getName());

        assertTrue(dao.findById("nope").isEmpty());
    }

    @Test
    void update_actualiza_campos_base() {
        Product p2 = dao.findById("p2").orElseThrow();
        p2.setName("Carta Pikachu (Updated)");
        p2.setStock(7);
        dao.update(p2);

        Product again = dao.findById("p2").orElseThrow();
        assertEquals("Carta Pikachu (Updated)", again.getName());
        assertEquals(7, again.getStock());
    }

    @Test
    void saveOrUpdateOffer_inserta_y_actualiza_oferta() {
        String todayPlus2 = LocalDate.now().plusDays(2).toString();

        dao.saveOrUpdateOffer("p1", 399.00, todayPlus2);
        Product p1 = dao.findById("p1").orElseThrow();
        assertEquals(399.00, p1.getPromoPrice(), 0.001);
        assertEquals(todayPlus2, p1.getValidUntil());

        String todayPlus5 = LocalDate.now().plusDays(5).toString();
        dao.saveOrUpdateOffer("p1", 379.50, todayPlus5);

        Product p1Upd = dao.findById("p1").orElseThrow();
        assertEquals(379.50, p1Upd.getPromoPrice(), 0.001);
        assertEquals(todayPlus5, p1Upd.getValidUntil());
    }

    // ===== NUEVO TEST AÑADIDO AQUÍ =====
    @Test
    void deleteOffer_elimina_si_existe() {
        String until = LocalDate.now().plusDays(3).toString();
        dao.saveOrUpdateOffer("p1", 399.00, until);

        // existe oferta
        Product withOffer = dao.findById("p1").orElseThrow();
        assertEquals(399.00, withOffer.getPromoPrice(), 0.001);

        // borrar
        dao.deleteOffer("p1");

        // ya no hay oferta
        Product withoutOffer = dao.findById("p1").orElseThrow();
        assertNull(withoutOffer.getPromoPrice());
        assertNull(withoutOffer.getValidUntil());
    }
}