# DECISIONS (Sprint 1)

> Documento vivo para registrar decisiones y cambios durante el desarrollo.

## 2025-10-24 – Elección de stack
- **Decisión:** Java 17 + Spark + Gson + Logback.
- **Alternativas:** Spring Boot, Javalin.
- **Justificación:** Spark reduce boilerplate y acelera el Sprint 1.

## 2025-10-24 – Modelo de datos
- **Decisión:** `User { id, name, email }` en memoria.
- **Justificación:** Suficiente para demostrar rutas CRUD sin overhead de BD.

## 2025-10-24 – Rutas
- **Decisión:** Seguir literalmente los endpoints del Sprint (incluyendo `POST /users/:id`).
- **Nota:** En REST clásico se usaría `POST /users` (sin `:id`), pero aquí se prioriza cumplir requerimiento.

## 2025-10-24 – CORS
- **Decisión:** Habilitar CORS global en dev.
- **Justificación:** Facilitar pruebas desde front (p.ej., `fetch` en navegador).

## Cambios posteriores
- _(Agrega fecha, descripción del cambio, motivo y efecto en el equipo)_.