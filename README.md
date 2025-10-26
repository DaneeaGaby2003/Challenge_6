# Spark Collectibles API (Sprint 1)

The **Spark Collectibles API** is a Java-based web service built with the **Spark framework**.  
It was developed as part of *Sprint 1*, inspired by the story of **Rafael**, a recent Systems Engineering graduate who collaborates with his friend **Ramón** to build an online collectibles store.  
This project implements the basic CRUD operations for managing users and demonstrates how Spark can be used to build lightweight REST APIs.

## Requirements
- Java 17+
- Maven 3.8+
- (Optional) Environment variable `PORT` to set a custom port (default `4567`)

## How to Build and Run
```bash
mvn clean package
java -jar target/spark-collectibles-api-1.0.0-shaded.jar
# or
mvn exec:java -Dexec.mainClass="com.example.App"


```bash
# Listar
curl -s http://localhost:4567/users | jq

# Obtener
curl -s http://localhost:4567/users/1 | jq

# Crear
curl -s -X POST http://localhost:4567/users/3   -H "Content-Type: application/json"   -d '{"name":"Ramón","email":"ramon@example.com"}' | jq

# Editar
curl -s -X PUT http://localhost:4567/users/3   -H "Content-Type: application/json"   -d '{"email":"ramon.organizer@example.com"}' | jq

# Verificar existencia
curl -i -X OPTIONS http://localhost:4567/users/3

# Eliminar
curl -s -X DELETE http://localhost:4567/users/3 | jq
```